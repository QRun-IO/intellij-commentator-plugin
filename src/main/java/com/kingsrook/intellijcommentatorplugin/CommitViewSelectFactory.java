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


import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.ListModel;
import java.awt.Component;
import java.awt.Container;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.CheckinProjectPanel;
import com.intellij.openapi.vcs.changes.CommitContext;
import com.intellij.openapi.vcs.checkin.CheckinHandler;
import com.intellij.openapi.vcs.checkin.CheckinHandlerFactory;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;


/*******************************************************************************
 ** todo wip - complete trash - but maybe could be a thing - a hotkey to run this maybe
 *******************************************************************************/
public class CommitViewSelectFactory extends CheckinHandlerFactory
{
   @Override
   public CheckinHandler createHandler(@NotNull CheckinProjectPanel panel, @NotNull CommitContext context)
   {
      return new CheckinHandler()
      {
         @Override
         @NotNull
         public ReturnResult beforeCheckin()
         {
            ApplicationManager.getApplication().invokeLater(() -> {
               Project       project   = panel.getProject();
               VirtualFile[] openFiles = FileEditorManager.getInstance(project).getSelectedFiles();
               if(openFiles.length == 0)
               {
                  return;
               }

               VirtualFile active = openFiles[0];

               JComponent root       = panel.getComponent();
               JList<?>   commitList = findJList(root);
               if(commitList != null)
               {
                  ListModel<?> model = commitList.getModel();
                  for(int i = 0; i < model.getSize(); i++)
                  {
                     Object      element = model.getElementAt(i);
                     VirtualFile vf      = extractVirtualFile(element);
                     if(vf != null && vf.equals(active))
                     {
                        commitList.setSelectedIndex(i);
                        commitList.ensureIndexIsVisible(i);
                        break;
                     }
                  }
               }
            });
            return ReturnResult.COMMIT;
         }
      };
   }



   // Recursively find the JList in the commit UI
   private JList<?> findJList(Component c)
   {
      if(c instanceof JList<?>)
      {
         return (JList<?>) c;
      }
      if(c instanceof Container container)
      {
         for(Component child : container.getComponents())
         {
            JList<?> result = findJList(child);
            if(result != null)
            {
               return result;
            }
         }
      }
      return null;
   }



   // Attempt to retrieve VirtualFile from the list element (e.g., Change or FilePath)
   private VirtualFile extractVirtualFile(Object element)
   {
      try
      {
         // adapt based on element types: Change, FilePath, etc.
         return (VirtualFile) element.getClass()
            .getMethod("getVirtualFile")
            .invoke(element);
      }
      catch(Exception ignored)
      {
         return null;
      }
   }
}
