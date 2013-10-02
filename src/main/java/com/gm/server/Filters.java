package com.gm.server;

import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;

public final class Filters {

  private Filters() {}
  
  public static final Filter neq(String property, Object value) {
    return new FilterPredicate(property, FilterOperator.NOT_EQUAL, value);
  }
  
  public static final Filter eq(String property, Object value) {
    return new FilterPredicate(property, FilterOperator.EQUAL, value);
  }
  
  public static final Filter gt(String property, Object value) {
    return new FilterPredicate(property, FilterOperator.GREATER_THAN, value);
  }

  public static final Filter gte(String property, Object value) {
    return new FilterPredicate(property, FilterOperator.GREATER_THAN_OR_EQUAL, value);
  }

  public static final Filter lt(String property, Object value) {
    return new FilterPredicate(property, FilterOperator.LESS_THAN, value);
  }

  public static final Filter lte(String property, Object value) {
    return new FilterPredicate(property, FilterOperator.LESS_THAN_OR_EQUAL, value);
  }
  
  public static final Filter in(String property, Object value) {
    return new FilterPredicate(property, FilterOperator.IN, value);
  }
  
  public static final Filter and(Filter... subFilters) {
    return CompositeFilterOperator.and(subFilters);
  }

  public static final Filter or(Filter... subFilters) {
    return CompositeFilterOperator.or(subFilters);
  }
}
