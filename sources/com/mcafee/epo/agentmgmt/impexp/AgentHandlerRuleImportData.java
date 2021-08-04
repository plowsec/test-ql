package com.mcafee.epo.agentmgmt.impexp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AgentHandlerRuleImportData {
    private List<RuleImportConflict> m_ruleList = new ArrayList();

    public List<RuleImportConflict> getRuleImportActions() {
        return Collections.unmodifiableList(this.m_ruleList);
    }

    public void addRuleImportAction(String str, String str2, boolean z) {
        RuleImportConflict ruleImportConflict = new RuleImportConflict();
        ruleImportConflict.setId(Integer.parseInt(str));
        ruleImportConflict.setName(str2);
        ruleImportConflict.setConflict(z);
        ruleImportConflict.setSelected(false);
        this.m_ruleList.add(ruleImportConflict);
    }

    public void clearRuleImportActions() {
        this.m_ruleList.clear();
    }

    public boolean existsInSelectedRuleImportActions(String str) {
        for (RuleImportConflict next : this.m_ruleList) {
            if (next.getSelected() && next.getName().equals(str)) {
                return true;
            }
        }
        return false;
    }

    public class RuleImportConflict {
        private boolean m_conflict;
        private int m_id;
        private String m_name;
        private boolean m_select;

        public RuleImportConflict() {
        }

        public int getId() {
            return this.m_id;
        }

        public void setId(int i) {
            this.m_id = i;
        }

        public String getName() {
            return this.m_name;
        }

        public void setName(String str) {
            this.m_name = str;
        }

        public boolean getConflict() {
            return this.m_conflict;
        }

        public void setConflict(boolean z) {
            this.m_conflict = z;
        }

        public boolean getSelected() {
            return this.m_select;
        }

        public void setSelected(boolean z) {
            this.m_select = z;
        }
    }
}
