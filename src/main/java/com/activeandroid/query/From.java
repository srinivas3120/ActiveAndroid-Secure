//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.activeandroid.query;

import com.activeandroid.Cache;
import com.activeandroid.Model;
import com.activeandroid.query.Join.JoinType;
import com.activeandroid.util.SQLiteUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public final class From implements Sqlable {
  private Sqlable mQueryBase;
  private Class<? extends Model> mType;
  private String mAlias;
  private List<Join> mJoins;
  private String mWhere;
  private String mGroupBy;
  private String mHaving;
  private String mOrderBy;
  private String mLimit;
  private String mOffset;
  private List<Object> mArguments;

  public From(Class<? extends Model> table, Sqlable queryBase) {
    this.mType = table;
    this.mJoins = new ArrayList();
    this.mQueryBase = queryBase;
    this.mJoins = new ArrayList();
    this.mArguments = new ArrayList();
  }

  public From as(String alias) {
    this.mAlias = alias;
    return this;
  }

  public Join join(Class<? extends Model> table) {
    Join join = new Join(this, table, (JoinType)null);
    this.mJoins.add(join);
    return join;
  }

  public Join leftJoin(Class<? extends Model> table) {
    Join join = new Join(this, table, JoinType.LEFT);
    this.mJoins.add(join);
    return join;
  }

  public Join outerJoin(Class<? extends Model> table) {
    Join join = new Join(this, table, JoinType.OUTER);
    this.mJoins.add(join);
    return join;
  }

  public Join innerJoin(Class<? extends Model> table) {
    Join join = new Join(this, table, JoinType.INNER);
    this.mJoins.add(join);
    return join;
  }

  public Join crossJoin(Class<? extends Model> table) {
    Join join = new Join(this, table, JoinType.CROSS);
    this.mJoins.add(join);
    return join;
  }

  public From where(String where) {
    this.mWhere = where;
    this.mArguments.clear();
    return this;
  }

  public From where(String where, Object... args) {
    this.mWhere = where;
    this.mArguments.clear();
    this.mArguments.addAll(Arrays.asList(args));
    return this;
  }

  public From groupBy(String groupBy) {
    this.mGroupBy = groupBy;
    return this;
  }

  public From having(String having) {
    this.mHaving = having;
    return this;
  }

  public From orderBy(String orderBy) {
    this.mOrderBy = orderBy;
    return this;
  }

  public From limit(int limit) {
    return this.limit(String.valueOf(limit));
  }

  public From limit(String limit) {
    this.mLimit = limit;
    return this;
  }

  public From offset(int offset) {
    return this.offset(String.valueOf(offset));
  }

  public From offset(String offset) {
    this.mOffset = offset;
    return this;
  }

  void addArguments(Object[] args) {
    this.mArguments.addAll(Arrays.asList(args));
  }

  public String toSql() {
    String sql = "";
    sql = sql + this.mQueryBase.toSql();
    sql = sql + "FROM " + Cache.getTableNameWithDb(mType) + " ";
    if(this.mAlias != null) {
      sql = sql + "AS " + this.mAlias + " ";
    }

    Join join;
    for(Iterator var3 = this.mJoins.iterator(); var3.hasNext(); sql = sql + join.toSql()) {
      join = (Join)var3.next();
    }

    if(this.mWhere != null) {
      sql = sql + "WHERE " + this.mWhere + " ";
    }

    if(this.mGroupBy != null) {
      sql = sql + "GROUP BY " + this.mGroupBy + " ";
    }

    if(this.mHaving != null) {
      sql = sql + "HAVING " + this.mHaving + " ";
    }

    if(this.mOrderBy != null) {
      sql = sql + "ORDER BY " + this.mOrderBy + " ";
    }

    if(this.mLimit != null) {
      sql = sql + "LIMIT " + this.mLimit + " ";
    }

    if(this.mOffset != null) {
      sql = sql + "OFFSET " + this.mOffset + " ";
    }

    return sql.trim();
  }

  public <T extends Model> List<T> execute() {
    if(this.mQueryBase instanceof Select) {
      return SQLiteUtils.rawQuery(this.mType, this.toSql(), this.getArguments());
    } else {
      SQLiteUtils.execSql(this.toSql(), this.getArguments());
      return null;
    }
  }

  public <T extends Model> T executeSingle() {
    if(this.mQueryBase instanceof Select) {
      this.limit(1);
      return SQLiteUtils.rawQuerySingle(this.mType, this.toSql(), this.getArguments());
    } else {
      SQLiteUtils.execSql(this.toSql(), this.getArguments());
      return null;
    }
  }

  private String[] getArguments() {
    int size = this.mArguments.size();
    String[] args = new String[size];

    for(int i = 0; i < size; ++i) {
      args[i] = this.mArguments.get(i).toString();
    }

    return args;
  }
}
