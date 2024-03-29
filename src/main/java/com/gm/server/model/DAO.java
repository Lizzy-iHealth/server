package com.gm.server.model;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.gm.server.model.Persistable.PropertySpec;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.PreparedQuery.TooManyResultsException;
import com.google.appengine.api.datastore.PropertyProjection;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Transaction;
import com.google.appengine.api.datastore.TransactionOptions;
import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

public final class DAO {

  private static final Logger logger = Logger.getLogger(DAO.class.getName());

  private final static DAO instance = new DAO();

  public final DatastoreService datastore = DatastoreServiceFactory
      .getDatastoreService();

  private Transaction transaction;

  private DAO() {
  }

  public static DAO get() {
    return instance;
  }

  public synchronized void begin(boolean withXG) {
    if(!withXG){
      begin();
      return;
          }
    
    Preconditions.checkArgument(transaction == null);
    
    TransactionOptions options = TransactionOptions.Builder.withXG(true);
    
    transaction = datastore.beginTransaction(options);
  }
  
  public synchronized void begin() {
    Preconditions.checkArgument(transaction == null);
    transaction = datastore.beginTransaction();
  }

  public synchronized void commit() {
    try{
    transaction.commit();
    }finally{
      if(transaction.isActive());
      transaction.rollback();
    }
    transaction = null;
  }

  public Key create(Persistable<?> object) {
    object.entity = null;
    return save(object);
  }
  public Key create(Persistable<?> object,Key parentKey) {
    object.entity = null;
    return save(object,parentKey);
  }

  public Key save(Persistable<?> object, Key parentKey) {
    if (object.entity == null) {
      // create new entity
      object.entity = new Entity(object.getKind(),parentKey);
    }
    return populatePropertiesAndSave(object); 
  }

  public Key save(Persistable<?> object) {
    if (object.entity == null) {
      // create new entity
      object.entity = new Entity(object.getKind());
    }
    return populatePropertiesAndSave(object);
  }

  public void read(Persistable<?> object) {
    if (object.entity == null) {
      throw new ModelException("Key missing for " + object);
    }
    try {
      Entity entity = datastore.get(object.entity.getKey());
      for (Map.Entry<String, PropertySpec> entry : Persistable.getProperties(
          object.getClass()).entrySet()) {
        entry.getValue().set(object, entity.getProperty(entry.getKey()));
      }
      object.entity = entity;
    } catch (EntityNotFoundException e) {
      throw new ModelException("Entity not found " + object, e);
    }
  }

  public void delete(Persistable<?> object) {
    if (object.entity == null) {
      throw new ModelException("Key missing for " + object);
    }
    datastore.delete(object.entity.getKey());
  }

  private Key populatePropertiesAndSave(Persistable<?> object) {
    object.touch();
    for (Map.Entry<String, PropertySpec> entry : Persistable.getProperties(
        object.getClass()).entrySet()) {
      object.entity.setProperty(entry.getKey(), entry.getValue().get(object));
    }
    return datastore.put(object.entity);
  }

  public <T extends Persistable<?>> T querySingle(String property,
      Object value, Class<T> type) {
    try {
      return query(type).filterBy(Filters.eq(property, value)).prepare()
          .asSingle();
    } catch (Exception e) {
      logger.log(Level.INFO, "Reading single error: " + e.getMessage(), e);
      return null;
    }
  }

  public <T extends Persistable<?>> T querySingle(String property,
      Object value, Class<T> type, Key parentKey) {
    try {
      return query(type).setAncestor(parentKey).filterBy(Filters.eq(property, value)).prepare()
          .asSingle();
    } catch (Exception e) {
      logger.log(Level.INFO, "Reading single error: " + e.getMessage(), e);
      return null;
    }
  }
  public <T extends Persistable<?>> T querySingle(Class<T> type, Key parentKey) {
    try {
      return query(type).setAncestor(parentKey).prepare()
          .asSingle();
    } catch (Exception e) {
      logger.log(Level.INFO, "Reading single error: " + e.getMessage(), e);
      return null;
    }
  }
  public <T extends Persistable<?>> QueryBuilder<T> query(Class<T> type) {
    if (Persistable.getProperties(type) == null) {
      try {
        type.getConstructor().newInstance();
        logger.info("instantiate type " + type);
      } catch (Exception e) {
        logger.log(Level.INFO, "error when instantiate type " + type, e);
      }
    }

    Query q = new Query(Persistable.nameOf(type));

    // Projection setup
    for (Map.Entry<String, PropertySpec> entry : Persistable
        .getProperties(type).entrySet()) {
      Class<?> propertyType = entry.getValue().type();
      if (propertyType == Date.class) {
        q.addProjection(new PropertyProjection(entry.getKey(), type));
      }
    }

    return new QueryBuilder<T>(type, q);
  }

  public int count(Class<? extends Persistable<?>> type) {
    return query(type).prepare().count();
  }
  
  public int count(Class<? extends Persistable<?>> type, Key parent) {
    return query(type).setAncestor(parent).prepare().count();
  }

  public <T extends Persistable<?>> T get(Key key, Class<T> type) {
    try {
      Entity entity = datastore.get(key);
      if (entity != null) {
        T object = Persistable.newInstance(type);
        for (Map.Entry<String, PropertySpec> entry : Persistable.getProperties(
            object.getClass()).entrySet()) {
          entry.getValue().set(object, entity.getProperty(entry.getKey()));
        }
        object.entity = entity;
        return object;
      }
    } catch (EntityNotFoundException e) {
      return null;
    }
    return null;
  }
  
  
  public <T extends Persistable<?>> T get(String key, Class<T> type) {
    try {
      Entity entity = datastore.get(KeyFactory.stringToKey(key));
      if (entity != null) {
        T object = Persistable.newInstance(type);
        for (Map.Entry<String, PropertySpec> entry : Persistable.getProperties(
            object.getClass()).entrySet()) {
          entry.getValue().set(object, entity.getProperty(entry.getKey()));
        }
        object.entity = entity;
        return object;
      }
    } catch (EntityNotFoundException e) {
      return null;
    }
    return null;
  }
  public final class QueryBuilder<T extends Persistable<?>> {

    private final Class<T> type;
    private final Query q;

    QueryBuilder(Class<T> type, Query q) {
      this.type = type;
      this.q = q;
    }
    public QueryBuilder<T> setAncestor(Key ancestor) {
      q.setAncestor(ancestor);
      return this;
    }

    public QueryBuilder<T> filterBy(Filter filter) {
      q.setFilter(filter);
      return this;
    }

    public QueryBuilder<T> sortBy(String propertyName, boolean desc) {
      q.addSort(propertyName, desc ? Query.SortDirection.DESCENDING
          : Query.SortDirection.ASCENDING);
      return this;
    }

    public QueryBuilder<T> distinct(boolean distinct) {
      q.setDistinct(distinct);
      return this;
    }

    public QueryResult<T> prepare() {
      return new QueryResult<T>(type, datastore.prepare(q));
    }
  }

  public final class QueryResult<T extends Persistable<?>> {

    private final Class<T> type;
    private final PreparedQuery pq;

    QueryResult(Class<T> type, PreparedQuery pq) {
      this.type = type;
      this.pq = pq;
    }

    public Iterable<T> asIterable() {
      return Iterables.transform(pq.asIterable(), new Function<Entity, T>() {
        @Override
        public T apply(Entity arg0) {
          return convert(arg0, Persistable.newInstance(type));
        }
      });
    }

    public Iterable<T> asIterable(FetchOptions arg0) {
      return Iterables.transform(pq.asIterable(arg0),
          new Function<Entity, T>() {
            @Override
            public T apply(Entity arg0) {
              return convert(arg0, Persistable.newInstance(type));
            }
          });
    }

    public List<T> asList() {
      return asList(FetchOptions.Builder.withDefaults());
    }

    public List<T> asList(FetchOptions arg0) {
      List<T> list = Lists.newArrayList();
      for (Entity entity : pq.asList(arg0)) {
        list.add(convert(entity, Persistable.newInstance(type)));
      }
      return list;
    }

    public T asSingle() throws TooManyResultsException {
      return convert(pq.asSingleEntity(), Persistable.newInstance(type));
    }

    public int count(FetchOptions arg0) {
      return pq.countEntities(arg0);
    }

    public int count() {
      return count(FetchOptions.Builder.withDefaults());
    }
    
    

    private T convert(Entity entity, T object) {
      if (entity != null) {
        for (Map.Entry<String, PropertySpec> entry : Persistable.getProperties(
            type).entrySet()) {
          entry.getValue().set(object, entity.getProperty(entry.getKey()));
        }
        object.entity = entity;
        return object;
      } else {
        return null;
      }
    }
  }
}
