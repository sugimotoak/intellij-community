/*
 * Copyright (c) 2000-2005 by JetBrains s.r.o. All Rights Reserved.
 * Use is subject to license terms.
 */
package com.intellij.codeInsight.completion;

import com.intellij.codeInsight.ExpectedTypeInfo;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;

/**
 * @author peter
 */
public class RecursiveCallParameterWeigher extends CompletionWeigher {

  public Integer weigh(@NotNull final LookupElement<?> element, final CompletionLocation location) {
    if (location.getCompletionType() != CompletionType.BASIC && location.getCompletionType() != CompletionType.SMART) return 0;

    final Object object = element.getObject();
    if (!(object instanceof PsiModifierListOwner) && !(object instanceof PsiExpression)) return 0;

    final PsiMethod positionMethod = JavaCompletionUtil.POSITION_METHOD.getValue(location);
    if (positionMethod == null) return 0;

    final PsiElement position = location.getCompletionParameters().getPosition();
    final PsiMethodCallExpression expression = PsiTreeUtil.getParentOfType(position, PsiMethodCallExpression.class, true, PsiClass.class);
    final PsiReferenceExpression reference = expression != null ? expression.getMethodExpression() : PsiTreeUtil.getParentOfType(position, PsiReferenceExpression.class);
    if (reference == null) return 0;

    final PsiExpression qualifier = reference.getQualifierExpression();
    boolean isDelegate = qualifier != null && !(qualifier instanceof PsiThisExpression) && !(qualifier instanceof PsiSuperExpression);
    if (expression != null) {
      final ExpectedTypeInfo[] expectedInfos = JavaCompletionUtil.EXPECTED_TYPES.getValue(location);
      if (expectedInfos != null) {
        final PsiType itemType = JavaCompletionUtil.getPsiType(object);
        if (itemType != null) {
          for (final ExpectedTypeInfo expectedInfo : expectedInfos) {
            if (positionMethod.equals(expectedInfo.getCalledMethod()) && expectedInfo.getType().isAssignableFrom(itemType)) {
              return isDelegate ? 2 : -1;
            }
          }
        }
      }
      return 0;
    }

    if (PsiTreeUtil.isAncestor(positionMethod, position, false) && PsiTreeUtil.isAncestor(reference, position, false)) {
      return isDelegate ? 2 : -1;
    }

    return 1;
  }
}
