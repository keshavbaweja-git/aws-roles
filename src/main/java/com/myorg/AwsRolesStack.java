package com.myorg;

import software.amazon.awscdk.CfnOutput;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.iam.*;
import software.constructs.Construct;

import java.util.HashMap;
import java.util.Map;

import static java.util.Arrays.asList;
import static software.amazon.awscdk.services.iam.Effect.*;

public class AwsRolesStack extends Stack {

    public AwsRolesStack(final Construct scope, final String id) {
        this(scope, id, null);
    }

    public AwsRolesStack(final Construct scope, final String id, final StackProps props) {
        super(scope, id, props);

        final String LAKE_FORMATION_WORKFLOW_ROLE = "LakeFormationWorkflowRole";

        final Role ssmManagedInstanceRole = Role.Builder
                .create(this, "ssmManagedInstanceRole")
                .roleName("SSMManagedInstanceRole")
                .assumedBy(new ServicePrincipal("ec2.amazonaws.com"))
                .managedPolicies(asList(ManagedPolicy.fromAwsManagedPolicyName("AmazonSSMManagedInstanceCore")))
                .build();
        CfnOutput.Builder
                .create(this, "ssmManagedInstanceRoleArn")
                .exportName("SSMManagedInstanceRoleArn")
                .value(ssmManagedInstanceRole.getRoleArn())
                .build();
        final PolicyDocument lakeFormationWorkflowPolicyDocument = PolicyDocument.Builder
                .create()
                .statements(asList(PolicyStatement
                                .Builder
                                .create()
                                .effect(ALLOW)
                                .actions(asList("lakeformation:GetDataAccess",
                                        "lakeformation:GrantPermissions"))
                                .resources(asList("*"))
                                .build(),
                        PolicyStatement
                                .Builder
                                .create()
                                .effect(ALLOW)
                                .actions(asList("iam:PassRole"))
                                .resources(asList("arn:aws:iam::"
                                        + this.getAccount()
                                        + ":role/" + LAKE_FORMATION_WORKFLOW_ROLE))
                                .build()))
                .build();
        final Map<String, PolicyDocument> lakeFormationWorkflowPolicyDocuments = new HashMap<>();
        lakeFormationWorkflowPolicyDocuments.put("LakeFormationWorkflow",
                lakeFormationWorkflowPolicyDocument);
        final Role lakeFormationWorkflowRole = Role.Builder
                .create(this, "lakeFormationWorkflowRole")
                .roleName(LAKE_FORMATION_WORKFLOW_ROLE)
                .assumedBy(new ServicePrincipal("glue.amazonaws.com"))
                .managedPolicies(asList(ManagedPolicy.fromAwsManagedPolicyName("service-role/AWSGlueServiceRole")))
                .inlinePolicies(lakeFormationWorkflowPolicyDocuments)
                .build();
        CfnOutput.Builder
                .create(this, "lakeFormationWorkflowRoleArn")
                .exportName(LAKE_FORMATION_WORKFLOW_ROLE + "Arn")
                .value(lakeFormationWorkflowRole.getRoleArn())
                .build();
    }
}
