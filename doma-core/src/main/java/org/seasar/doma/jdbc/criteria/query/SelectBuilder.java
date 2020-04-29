package org.seasar.doma.jdbc.criteria.query;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import org.seasar.doma.def.EntityDef;
import org.seasar.doma.def.PropertyDef;
import org.seasar.doma.internal.jdbc.sql.PreparedSqlBuilder;
import org.seasar.doma.internal.util.Pair;
import org.seasar.doma.jdbc.Config;
import org.seasar.doma.jdbc.PreparedSql;
import org.seasar.doma.jdbc.SqlKind;
import org.seasar.doma.jdbc.SqlLogType;
import org.seasar.doma.jdbc.criteria.context.Criterion;
import org.seasar.doma.jdbc.criteria.context.Join;
import org.seasar.doma.jdbc.criteria.context.JoinKind;
import org.seasar.doma.jdbc.criteria.context.SelectContext;

public class SelectBuilder {
  private final SelectContext context;
  private final Function<String, String> commenter;
  private final PreparedSqlBuilder buf;
  private final BuilderSupport support;

  public SelectBuilder(
      Config config,
      SelectContext context,
      Function<String, String> commenter,
      SqlLogType sqlLogType) {
    this(
        config,
        context,
        commenter,
        new PreparedSqlBuilder(config, SqlKind.SELECT, sqlLogType),
        new AliasManager(context));
  }

  public SelectBuilder(
      Config config,
      SelectContext context,
      Function<String, String> commenter,
      PreparedSqlBuilder buf,
      AliasManager aliasManager) {
    Objects.requireNonNull(config);
    Objects.requireNonNull(context);
    Objects.requireNonNull(commenter);
    Objects.requireNonNull(buf);
    Objects.requireNonNull(aliasManager);
    this.context = context;
    this.commenter = commenter;
    this.buf = buf;
    support = new BuilderSupport(config, commenter, buf, aliasManager);
  }

  public PreparedSql build() {
    interpret();
    return buf.build(commenter);
  }

  void interpret() {
    select();
    from();
    where();
    groupBy();
    having();
    orderBy();
    limit();
    offset();
    forUpdate();
  }

  private void select() {
    buf.appendSql("select ");

    if (context.distinct) {
      buf.appendSql("distinct ");
    }

    List<PropertyDef<?>> propertyDefs = context.allPropertyDefs();
    if (propertyDefs.isEmpty()) {
      buf.appendSql("*");
    } else {
      for (PropertyDef<?> propertyDef : propertyDefs) {
        column(propertyDef);
        buf.appendSql(", ");
      }
      buf.cutBackSql(2);
    }
  }

  private void from() {
    buf.appendSql(" from ");
    table(context.entityDef);
    if (!context.joins.isEmpty()) {
      for (Join join : context.joins) {
        if (join.kind == JoinKind.INNER) {
          buf.appendSql(" inner join ");
        } else if (join.kind == JoinKind.LEFT) {
          buf.appendSql(" left outer join ");
        }
        table(join.entityDef);
        if (!join.on.isEmpty()) {
          buf.appendSql(" on (");
          int index = 0;
          for (Criterion criterion : join.on) {
            visitCriterion(index, criterion);
            index++;
            buf.appendSql(" and ");
          }
          buf.cutBackSql(5);
          buf.appendSql(")");
        }
      }
    }
  }

  private void where() {
    if (!context.where.isEmpty()) {
      buf.appendSql(" where ");
      int index = 0;
      for (Criterion criterion : context.where) {
        visitCriterion(index++, criterion);
        buf.appendSql(" and ");
      }
      buf.cutBackSql(5);
    }
  }

  private void groupBy() {
    if (!context.groupBy.isEmpty()) {
      buf.appendSql(" group by ");
      for (PropertyDef<?> p : context.groupBy) {
        column(p);
        buf.appendSql(", ");
      }
      buf.cutBackSql(2);
    }
  }

  private void having() {
    if (!context.having.isEmpty()) {
      buf.appendSql(" having ");
      int index = 0;
      for (Criterion criterion : context.having) {
        visitCriterion(index++, criterion);
        buf.appendSql(" and ");
      }
      buf.cutBackSql(5);
    }
  }

  private void orderBy() {
    if (!context.orderBy.isEmpty()) {
      buf.appendSql(" order by ");
      for (Pair<PropertyDef<?>, String> pair : context.orderBy) {
        column(pair.fst);
        buf.appendSql(" " + pair.snd + ", ");
      }
      buf.cutBackSql(2);
    }
  }

  private void limit() {
    if (context.limit != null) {
      buf.appendSql(" limit ");
      buf.appendSql(context.limit.toString());
    }
  }

  private void offset() {
    if (context.offset != null) {
      buf.appendSql(" offset ");
      buf.appendSql(context.offset.toString());
    }
  }

  private void forUpdate() {
    if (context.forUpdate != null) {
      buf.appendSql(" for update");
      if (context.forUpdate.nowait) {
        buf.appendSql(" nowait");
      }
    }
  }

  private void table(EntityDef<?> entityDef) {
    support.table(entityDef);
  }

  private void column(PropertyDef<?> propertyDef) {
    support.column(propertyDef);
  }

  private void visitCriterion(int index, Criterion criterion) {
    support.visitCriterion(index, criterion);
  }
}
