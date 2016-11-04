/*
 * Copyright (c) 2014, 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.learningswitch;

import java.util.ArrayList;
import java.util.List;

import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Uri;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.OutputActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.output.action._case.OutputActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.ActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.ActionKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowModFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.OutputPortValues;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.InstructionsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.ApplyActionsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.apply.actions._case.ApplyActions;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.apply.actions._case.ApplyActionsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.Instruction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.InstructionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.InstructionKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnectorKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.ethernet.match.fields.EthernetDestinationBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.ethernet.match.fields.EthernetSourceBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.EthernetMatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.EthernetMatchBuilder;

import com.google.common.collect.ImmutableList;

public class FlowUtils {
    private FlowUtils() {
        //prohibite to instantiate util class
    }

    /**
     * @param tableId
     * @param priority
     * @param srcMac
     * @param dstMac
     * @param dstPort
     * @return {@link FlowBuilder} forwarding all packets to controller port
     */
    public static FlowBuilder createDirectMacToMacFlow(final Short tableId, final int priority, final MacAddress srcMac,
            final MacAddress dstMac, final NodeConnectorRef dstPort) {
        FlowBuilder macToMacFlow = new FlowBuilder()
                .setTableId(tableId)
                .setFlowName("mac2mac");
        macToMacFlow.setId(new FlowId(Long.toString(macToMacFlow.hashCode())));

        EthernetMatch ethernetMatch = new EthernetMatchBuilder()
                .setEthernetSource(new EthernetSourceBuilder()
                        .setAddress(srcMac)
                        .build())
                .setEthernetDestination(new EthernetDestinationBuilder()
                        .setAddress(dstMac)
                        .build())
                .build();

        MatchBuilder match = new MatchBuilder();
        match.setEthernetMatch(ethernetMatch);

        Uri outputPort = dstPort.getValue().firstKeyOf(NodeConnector.class, NodeConnectorKey.class).getId();

        Action outputToControllerAction = new ActionBuilder()
                .setOrder(0)
                .setAction(new OutputActionCaseBuilder()
                        .setOutputAction(new OutputActionBuilder()
                                .setMaxLength(Integer.valueOf(0xffff))
                                .setOutputNodeConnector(outputPort)
                                .build())
                        .build())
                .build();

        // Create an Apply Action
        ApplyActions applyActions = new ApplyActionsBuilder().setAction(ImmutableList.of(outputToControllerAction))
                .build();

        // Wrap our Apply Action in an Instruction
        Instruction applyActionsInstruction = new InstructionBuilder()
                .setOrder(0)
                .setInstruction(new ApplyActionsCaseBuilder()
                        .setApplyActions(applyActions)
                        .build())
                .build();

        // Put our Instruction in a list of Instructions

        macToMacFlow
                .setMatch(new MatchBuilder()
                        .setEthernetMatch(ethernetMatch)
                        .build())
                .setInstructions(new InstructionsBuilder()
                        .setInstruction(ImmutableList.of(applyActionsInstruction))
                        .build())
                .setPriority(priority)
                .setBufferId(OFConstants.OFP_NO_BUFFER)
                .setHardTimeout(0)
                .setIdleTimeout(0)
                .setFlags(new FlowModFlags(false, false, false, false, false));

        return macToMacFlow;
    }

    /**
     * @param tableId
     * @param priority
     * @param flowId
     * @return {@link FlowBuilder} forwarding all packets to controller port
     */
    public static FlowBuilder createFwdAllToControllerFlow(final Short tableId, final int priority, final FlowId flowId) {
        FlowBuilder allToCtrlFlow = new FlowBuilder().setTableId(tableId).setFlowName("allPacketsToCtrl").setId(flowId)
                .setKey(new FlowKey(flowId));

        MatchBuilder matchBuilder = new MatchBuilder();

        // Create output action -> send to controller
        OutputActionBuilder output = new OutputActionBuilder();
        output.setMaxLength(Integer.valueOf(0xffff));
        Uri controllerPort = new Uri(OutputPortValues.CONTROLLER.toString());
        output.setOutputNodeConnector(controllerPort);

        ActionBuilder ab = new ActionBuilder();
        ab.setAction(new OutputActionCaseBuilder().setOutputAction(output.build()).build());
        ab.setOrder(0);
        ab.setKey(new ActionKey(0));

        List<Action> actionList = new ArrayList<Action>();
        actionList.add(ab.build());

        // Create an Apply Action
        ApplyActionsBuilder aab = new ApplyActionsBuilder();
        aab.setAction(actionList);

        // Wrap our Apply Action in an Instruction
        InstructionBuilder ib = new InstructionBuilder();
        ib.setInstruction(new ApplyActionsCaseBuilder().setApplyActions(aab.build()).build());
        ib.setOrder(0);
        ib.setKey(new InstructionKey(0));

        // Put our Instruction in a list of Instructions
        InstructionsBuilder isb = new InstructionsBuilder();
        List<Instruction> instructions = new ArrayList<Instruction>();
        instructions.add(ib.build());
        isb.setInstruction(instructions);

        allToCtrlFlow
            .setMatch(matchBuilder.build())
            .setInstructions(isb.build())
            .setPriority(priority)
            .setBufferId(OFConstants.OFP_NO_BUFFER)
            .setHardTimeout(0)
            .setIdleTimeout(0)
            .setFlags(new FlowModFlags(false, false, false, false, false));

        return allToCtrlFlow;
    }
}
