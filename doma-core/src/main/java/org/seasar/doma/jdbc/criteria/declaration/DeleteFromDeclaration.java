package org.seasar.doma.jdbc.criteria.declaration;

import java.util.Objects;
import java.util.function.Consumer;
import org.seasar.doma.jdbc.criteria.context.DeleteContext;

public class DeleteFromDeclaration {

  private final DeleteContext context;

  public DeleteFromDeclaration(DeleteContext context) {
    Objects.requireNonNull(context);
    this.context = context;
  }

  public DeleteContext getContext() {
    return context;
  }

  public void where(Consumer<WhereDeclaration> block) {
    Objects.requireNonNull(block);
    WhereDeclaration declaration = new WhereDeclaration(context);
    block.accept(declaration);
  }
}
