package com.gm.server.model;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Map;

import com.google.appengine.api.datastore.Key;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

public abstract class Persistable<T extends Persistable<?>> {
  
  public static final class PropertySpec {
    
    private final Field field;
    
    PropertySpec(Field field) {
      this.field = field;
      field.setAccessible(true);
    }
    
    public Object get(Object instance) {
      try {
        return field.get(instance);
      } catch (Exception e) {
        throw new ModelException(e);
      }
    }
    
    public void set(Object instance, Object value) {
      try {
        field.set(instance, value);
      } catch (Exception e) {
        throw new ModelException(e);
      }
    }
    
    public Class<?> type() {
      return field.getDeclaringClass();
    }
  }
  
  static String nameOf(Class<?> type) {
    return entityToName.get(type);
  }
  
  static Map<String, PropertySpec> getProperties(Class<?> type) {
    return entityToProperties.get(type);
  }

  @SuppressWarnings("unchecked")
  static <T extends Persistable<?>> T newInstance(Class<T> type) {
    try {
      return (T) entityToConstructor.get(type).newInstance();
    } catch (Exception e) {
      throw new ModelException(e);
    }
  }
  
  @SuppressWarnings("rawtypes")
  private static final Map<Class<? extends Persistable>, Map<String, PropertySpec>> 
      entityToProperties = Maps.newHashMap();
  @SuppressWarnings("rawtypes")
  private static final Map<Class<? extends Persistable>, String> entityToName = Maps.newHashMap();
  @SuppressWarnings("rawtypes")
  private static final Map<Class<? extends Persistable>, Constructor<?>> 
      entityToConstructor = Maps.newHashMap();
  
  com.google.appengine.api.datastore.Entity entity;
    
  protected Persistable() {
    if (!entityToProperties.containsKey(this.getClass())) {
      // TODO: this doesn't return fields inherited from super classes
      ImmutableMap.Builder<String, PropertySpec> properties = ImmutableMap.builder();
      for (Field field : this.getClass().getDeclaredFields()) {
        Property spec = field.getAnnotation(Property.class);
        if (spec != null) {
          String name = Strings.isNullOrEmpty(spec.value()) ? field.getName() : spec.value();
          field.setAccessible(true);
          properties.put(name, new PropertySpec(field));
        }
      }
      entityToProperties.put(this.getClass(), properties.build());
    }
    
    if (!entityToName.containsKey(this.getClass())) {
      String defined = this.getClass().getAnnotation(Entity.class).value();
      String name = Strings.isNullOrEmpty(defined) ? this.getClass().getSimpleName() : defined;
      entityToName.put(this.getClass(), name);
    }
    
    if (!entityToConstructor.containsKey(this.getClass())) {
      try {
        Constructor<?> ctr = getConstructor();
        ctr.setAccessible(true);
        entityToConstructor.put(this.getClass(), ctr);
      } catch (Exception e) {
        throw new ModelException("can't find the default constructor for " + this.getClass(), e);
      }
    }
  }
  
  Constructor<?> getConstructor() throws SecurityException, NoSuchMethodException {
    try {
      return this.getClass().getConstructor();
    } catch (Exception e) {
      return this.getClass().getDeclaredConstructor();
    }
  }

  String getKind() {
    return entityToName.get(this.getClass());
  }
  
  public abstract T touch();
  
  public Key save() {
    return DAO.get().save(this);
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder();
    buf.append(getKind());
    buf.append("{\n");
    for (Map.Entry<String, PropertySpec> entry : Persistable.getProperties(getClass()).entrySet()) {
      buf.append("  ");
      buf.append(entry.getKey());
      buf.append(":");
      buf.append(entry.getValue().get(this));
    }
    buf.append("\n}");
    return buf.toString();
  }
}