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


import com.intellij.lang.documentation.AbstractDocumentationProvider;
import com.intellij.lang.java.JavaDocumentationProvider;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.Nullable;


/*******************************************************************************
 * fix the rendering of javadocs in the ide, for when they come from one of our
 * header-comments that's full of stars
 *******************************************************************************/
public class CustomJavadocProvider extends AbstractDocumentationProvider
{
   private final JavaDocumentationProvider javaProvider = new JavaDocumentationProvider();



   /***************************************************************************
    *
    ***************************************************************************/
   @Override
   public String generateDoc(PsiElement element, @Nullable PsiElement originalElement)
   {
      /////////////////////////////////////////
      // Get the standard Java documentation //
      /////////////////////////////////////////
      String originalDoc = javaProvider.generateDoc(element, originalElement);

      if (originalDoc == null)
      {
         return null;
      }

      /////////////////////////////////////////////
      // Apply your custom formatting/processing //
      /////////////////////////////////////////////
      return processJavadoc(originalDoc);
   }



   /***************************************************************************
    * If the content starts with a line of stars, strip it away!
    ***************************************************************************/
   private String processJavadoc(String originalDoc)
   {
      return originalDoc.replaceFirst("""
         (<div class='content'>) *\\*{3,}""", "$1");
   }
}
