package com.mcafee.epo.agentmgmt.ah;

import com.mcafee.epo.agentmgmt.ah.AgentHandlerAssignmentImportDS;
import com.mcafee.orion.core.ui.DisplayAdapter;
import com.mcafee.orion.core.ui.PropertyAccessException;
import com.mcafee.orion.core.ui.control.table.TableCellRendererImpl;

public class AgentHandlerAssignmentImportCR extends TableCellRendererImpl {
    public String formatCell(DisplayAdapter displayAdapter, String str, Object obj) throws PropertyAccessException {
        String formatCell = AgentHandlerAssignmentImportCR.super.formatCell(displayAdapter, str, obj);
        if (!((AgentHandlerAssignmentImportDS.AssignmentImport) obj).getConflict()) {
            return formatCell;
        }
        if ("name".equals(str)) {
            return "<label class=\"errorText\">" + formatCell + "</label>";
        }
        if ("conflict".equals(str)) {
            return "<label class=\"errorText\">" + formatCell + "</label>";
        }
        return formatCell;
    }
}
