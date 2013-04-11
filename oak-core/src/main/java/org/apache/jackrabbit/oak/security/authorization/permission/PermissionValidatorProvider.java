/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.jackrabbit.oak.security.authorization.permission;

import java.security.AccessController;
import javax.annotation.Nonnull;
import javax.security.auth.Subject;

import org.apache.jackrabbit.oak.core.ImmutableTree;
import org.apache.jackrabbit.oak.core.TreeTypeProviderImpl;
import org.apache.jackrabbit.oak.spi.commit.Validator;
import org.apache.jackrabbit.oak.spi.commit.ValidatorProvider;
import org.apache.jackrabbit.oak.spi.security.Context;
import org.apache.jackrabbit.oak.spi.security.SecurityProvider;
import org.apache.jackrabbit.oak.spi.security.authorization.permission.PermissionProvider;
import org.apache.jackrabbit.oak.spi.state.NodeState;

/**
 * PermissionValidatorProvider... TODO
 */
public class PermissionValidatorProvider extends ValidatorProvider {

    private final SecurityProvider securityProvider;

    private Context acCtx;
    private Context userCtx;

    public PermissionValidatorProvider(SecurityProvider securityProvider) {
        this.securityProvider = securityProvider;
    }

    //--------------------------------------------------< ValidatorProvider >---
    @Nonnull
    @Override
    public Validator getRootValidator(NodeState before, NodeState after) {
        PermissionProvider pp = getPermissionProvider();
        return new PermissionValidator(createTree(before), createTree(after), pp, this);
    }

    //--------------------------------------------------------------------------

    Context getAccessControlContext() {
        if (acCtx == null) {
            acCtx = securityProvider.getAccessControlConfiguration().getContext();
        }
        return acCtx;
    }

    Context getUserContext() {
        if (userCtx == null) {
            userCtx = securityProvider.getUserConfiguration().getContext();
        }
        return userCtx;
    }

    private ImmutableTree createTree(NodeState root) {
        return new ImmutableTree(root, new TreeTypeProviderImpl(getAccessControlContext()));
    }

    private PermissionProvider getPermissionProvider() {
        Subject subject = Subject.getSubject(AccessController.getContext());
        if (subject == null || subject.getPublicCredentials(PermissionProvider.class).isEmpty()) {
            throw new IllegalStateException("Unable to validate permissions; no permission provider associated with the commit call.");
        } else {
            return subject.getPublicCredentials(PermissionProvider.class).iterator().next();
        }
    }
}
