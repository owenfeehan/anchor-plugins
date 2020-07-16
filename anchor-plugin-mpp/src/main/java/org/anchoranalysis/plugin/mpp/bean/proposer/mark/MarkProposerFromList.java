/* (C)2020 */
package org.anchoranalysis.plugin.mpp.bean.proposer.mark;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.anchoranalysis.anchor.mpp.bean.proposer.MarkProposer;
import org.anchoranalysis.anchor.mpp.mark.Mark;
import org.anchoranalysis.anchor.mpp.proposer.ProposalAbnormalFailureException;
import org.anchoranalysis.anchor.mpp.proposer.ProposerContext;
import org.anchoranalysis.anchor.mpp.proposer.visualization.CreateProposalVisualization;
import org.anchoranalysis.anchor.mpp.pxlmark.memo.VoxelizedMarkMemo;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.NonEmpty;

public abstract class MarkProposerFromList extends MarkProposer {

    // START BEAN PROPERTIES
    @BeanField @NonEmpty private List<MarkProposer> list = new ArrayList<>();
    // END BEAN PROPERTIES

    @Override
    public boolean propose(VoxelizedMarkMemo inputMark, ProposerContext context)
            throws ProposalAbnormalFailureException {
        return propose(inputMark, context, list);
    }

    @Override
    public boolean isCompatibleWith(Mark testMark) {

        for (MarkProposer markProposer : list) {

            if (!markProposer.isCompatibleWith(testMark)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public Optional<CreateProposalVisualization> proposalVisualization(boolean detailed) {
        return proposalVisualization(detailed, list);
    }

    protected abstract boolean propose(
            VoxelizedMarkMemo inputMark,
            ProposerContext context,
            List<MarkProposer> markProposerList)
            throws ProposalAbnormalFailureException;

    protected abstract Optional<CreateProposalVisualization> proposalVisualization(
            boolean detailed, List<MarkProposer> markProposerList);

    public List<MarkProposer> getList() {
        return list;
    }

    public void setList(List<MarkProposer> list) {
        this.list = list;
    }
}
