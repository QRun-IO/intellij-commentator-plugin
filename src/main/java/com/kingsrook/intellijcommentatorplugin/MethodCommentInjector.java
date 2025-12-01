/*
 * Kingsrook IntelliJ Commentator Plugin
 * Copyright (C) 2025.  Kingsrook, LLC
 * 651 N Broad St Ste 205 # 6917 | Middletown DE 19709 | United States
 * contact@kingsrook.com
 * https://github.com/Kingsrook/intellij-commentator-plugin
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.kingsrook.intellijcommentatorplugin;


import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiCodeBlock;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementFactory;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiParameter;
import com.intellij.psi.PsiParserFacade;
import com.intellij.psi.PsiStatement;
import com.intellij.psi.PsiTreeChangeAdapter;
import com.intellij.psi.PsiTreeChangeEvent;
import com.intellij.psi.PsiType;
import com.kingsrook.intellijcommentatorplugin.settings.CommentatorSettingsState;


/*******************************************************************************
 ** https://claude.ai/share/699ecf53-40ea-4516-ae8a-293e654523b8
 *******************************************************************************/
public class MethodCommentInjector extends PsiTreeChangeAdapter
{
   @Override
   public void childAdded(PsiTreeChangeEvent event)
   {
      PsiElement child = event.getChild();

      CommentatorSettingsState settings = CommentatorSettingsState.getInstance();
      if(!settings.autoWriteMethodHeaderCommentsField)
      {
         return;
      }

      if(child instanceof PsiMethod)
      {
         PsiMethod method = (PsiMethod) child;

         // Check if this is a new stub method (no body or empty body)
         if(isStubMethod(method))
         {
            addStarComment(method);
         }
      }
   }



   private boolean isStubMethod(PsiMethod method)
   {
      PsiCodeBlock body = method.getBody();

      // Interface methods have no body
      if(body == null)
      {
         return true;
      }

      // Check if body is empty or contains only generated stub code
      PsiStatement[] statements = body.getStatements();
      if(statements.length == 0)
      {
         return true;
      }

      // Check for typical stub patterns like "return null;" or "throw new UnsupportedOperationException();"
      if(statements.length == 1)
      {
         String text = statements[0].getText();
         return text.contains("return null") ||
            text.contains("UnsupportedOperationException") ||
            text.contains("TODO");
      }

      return false;
   }



   private void addStarComment(PsiMethod method)
   {
      Project project = method.getProject();

      WriteCommandAction.runWriteCommandAction(project, () -> {
         PsiElementFactory factory = JavaPsiFacade.getElementFactory(project);

         String commentText = generateStarComment(method);

         // Create a dummy method with the comment to parse it properly
         String    dummyCode   = commentText + "\nvoid dummy() {}";
         PsiMethod dummyMethod = factory.createMethodFromText(dummyCode, method);

         // Extract the comment from the dummy method
         PsiElement firstChild = dummyMethod.getFirstChild();
         while(firstChild != null && !(firstChild instanceof PsiComment))
         {
            firstChild = firstChild.getNextSibling();
         }

         if(firstChild instanceof PsiComment)
         {
            PsiComment comment = (PsiComment) firstChild.copy();

            // Add comment before the method
            PsiElement parent = method.getParent();
            parent.addBefore(comment, method);

            // Add newline after comment
            PsiElement whitespace = PsiParserFacade.getInstance(project)
               .createWhiteSpaceFromText("\n   ");
            parent.addBefore(whitespace, method);
         }
      });
   }



   private String generateStarComment(PsiMethod method)
   {
      StringBuilder  sb         = new StringBuilder();
      String         methodName = method.getName();
      PsiParameter[] params     = method.getParameterList().getParameters();
      PsiType        returnType = method.getReturnType();

      sb.append("""
         
         /***************************************************************************
          *
          ***************************************************************************/
         """);

      return sb.toString();
   }



   private String centerText(String text, int width)
   {
      int padding = (width - text.length()) / 2;
      return " ".repeat(padding) + text + " ".repeat(width - text.length() - padding);
   }
}
