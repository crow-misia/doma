package org.seasar.doma.jdbc.query;

import java.lang.reflect.Method;
import org.seasar.doma.internal.jdbc.entity.AbstractPostInsertContext;
import org.seasar.doma.internal.jdbc.entity.AbstractPreInsertContext;
import org.seasar.doma.internal.jdbc.sql.PreparedSqlBuilder;
import org.seasar.doma.jdbc.Config;
import org.seasar.doma.jdbc.Naming;
import org.seasar.doma.jdbc.SqlKind;
import org.seasar.doma.jdbc.dialect.Dialect;
import org.seasar.doma.jdbc.entity.EntityType;
import org.seasar.doma.jdbc.id.IdGenerationConfig;

public class AutoUpsertQuery<ENTITY> extends AutoInsertQuery<ENTITY> implements UpsertQuery {
  private DuplicateKeyType duplicateKeyType;

  public AutoUpsertQuery(EntityType<ENTITY> entityType) {
    super(entityType);
  }

  @Override
  protected void preInsert() {
    AutoPreUpsertContext<ENTITY> context =
        new AutoPreUpsertContext<>(entityType, method, config, duplicateKeyType);
    entityType.preInsert(entity, context);
    if (context.getNewEntity() != null) {
      entity = context.getNewEntity();
    }
  }

  @Override
  protected void prepareSpecialPropertyTypes() {
    super.prepareSpecialPropertyTypes();
    generatedIdPropertyType = entityType.getGeneratedIdPropertyType();
    if (generatedIdPropertyType != null) {
      idGenerationConfig = new IdGenerationConfig(config, entityType);
      generatedIdPropertyType.validateGenerationStrategy(idGenerationConfig);
      autoGeneratedKeysSupported =
          generatedIdPropertyType.isAutoGeneratedKeysSupported(idGenerationConfig);
    }
  }

  @Override
  protected void prepareSql() {
    Naming naming = config.getNaming();
    Dialect dialect = config.getDialect();
    PreparedSqlBuilder builder = new PreparedSqlBuilder(config, SqlKind.BATCH_UPSERT, sqlLogType);

    UpsertContext context =
        UpsertContext.fromEntity(
            builder,
            entityType,
            duplicateKeyType,
            naming,
            dialect,
            idPropertyTypes,
            targetPropertyTypes,
            entity);
    UpsertBuilder upsertBuilderQuery = dialect.getUpsertBuilder(context);
    upsertBuilderQuery.build();
    sql = builder.build(this::comment);
  }

  @Override
  protected void postInsert() {
    AutoPostUpsertContext<ENTITY> context =
        new AutoPostUpsertContext<>(entityType, method, config, duplicateKeyType);
    entityType.postInsert(entity, context);
    if (context.getNewEntity() != null) {
      entity = context.getNewEntity();
    }
  }

  @Override
  public void setDuplicateKeyType(DuplicateKeyType duplicateKeyType) {
    this.duplicateKeyType = duplicateKeyType;
  }

  protected static class AutoPreUpsertContext<E> extends AbstractPreInsertContext<E> {

    private final DuplicateKeyType duplicateKeyType;

    public AutoPreUpsertContext(
        EntityType<E> entityType, Method method, Config config, DuplicateKeyType duplicateKeyType) {
      super(entityType, method, config);
      this.duplicateKeyType = duplicateKeyType;
    }

    public java.util.Optional<DuplicateKeyType> getDuplicateKeyType() {
      return java.util.Optional.of(duplicateKeyType);
    }
  }

  protected static class AutoPostUpsertContext<E> extends AbstractPostInsertContext<E> {

    private final DuplicateKeyType duplicateKeyType;

    public AutoPostUpsertContext(
        EntityType<E> entityType, Method method, Config config, DuplicateKeyType duplicateKeyType) {
      super(entityType, method, config);
      this.duplicateKeyType = duplicateKeyType;
    }

    public java.util.Optional<DuplicateKeyType> getDuplicateKeyType() {
      return java.util.Optional.of(duplicateKeyType);
    }
  }
}
