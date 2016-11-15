/*
 * Copyright (c) 2016, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.c5.multitenancy.k8s;

import io.fabric8.kubernetes.api.model.Namespace;
import io.fabric8.kubernetes.api.model.NamespaceBuilder;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.ObjectMetaBuilder;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.utils.Strings;
import io.netty.handler.codec.http.HttpHeaders;
import okhttp3.internal.http.RetryAndFollowUpInterceptor;
import org.wso2.c5.multitenancy.ITenancyProvider;
import org.wso2.c5.multitenancy.models.Tenant;

import java.util.ArrayList;
import java.util.List;

/**
 * Tenant provider for Kubernetes cluster manager.
 */
public class KubernetesTenancyProvider implements ITenancyProvider {

    private KubernetesClient kubernetesClient;

    public KubernetesTenancyProvider() {
        String endpointIP = System.getenv(KubernetesConstants.KUBERNETES_MASTER_IP_ENV);
        String endpointPort = System.getenv(KubernetesConstants.KUBERNETES_MASTER_PORT_ENV);

        if (Strings.isNullOrBlank(endpointIP)) {
            endpointIP = KubernetesConstants.DEFAULT_KUBERNETES_MASTER_IP;
        }
        if (Strings.isNullOrBlank(endpointPort)) {
            endpointPort = KubernetesConstants.DEFAULT_KUBERNETES_MASTER_PORT;
        }
        this.kubernetesClient = new DefaultKubernetesClient("http://" + endpointIP + ":" + endpointPort);
    }

    @Override
    public Tenant[] getTenants() {
        List<Tenant> tenants = new ArrayList<>();
        for (Namespace ns : kubernetesClient.namespaces().list().getItems()) {
            String name = ns.getMetadata().getName();
            // Do not return reserved namespaces.
            if (!isReservedNamespace(name)) {
                tenants.add(new Tenant(name));
            }
        }
        return tenants.toArray(new Tenant[tenants.size()]);
    }

    @Override
    public Tenant getTenant(String name) {
        name = name.replace(".", "-").toLowerCase();
        for (Namespace ns : kubernetesClient.namespaces().list().getItems()) {
            if (ns.getMetadata().getName().equals(name)) {
                return new Tenant(ns.getMetadata().getName());
            }
        }
        return null;
    }

    @Override
    public boolean createTenant(Tenant tenant) {
        tenant.setName(tenant.getName().replace(".", "-").toLowerCase());
        if (isReservedNamespace(tenant.getName())) {
            // Unable to create a tenant with the system namespace name
            return false;
        }

        // Check whether the namespace already exists
        if (isNamespaceExists(tenant.getName())) {
            return false;
        }

        ObjectMeta meta = new ObjectMetaBuilder()
                .withName(tenant.getName())
                .build();
        Namespace ns = new NamespaceBuilder()
                .withMetadata(meta)
                .build();

        kubernetesClient.namespaces().create(ns);
        return true;
    }

    @Override
    public boolean deleteTenant(String name) {
        name = name.replace(".", "-").toLowerCase();

        if (isReservedNamespace(name)) {
            // Cannot delete system namespaces.
            return false;
        }

        for (Namespace ns : kubernetesClient.namespaces().list().getItems()) {
            if (ns.getMetadata().getName().equals(name)) {
                kubernetesClient.namespaces().delete(ns);
                return true;
            }
        }
        // Namespace not available
        return false;
    }

    private boolean isReservedNamespace(String namespace) {
        return (namespace.equals(KubernetesConstants.RESERVED_NAMESPACE_DEFAULT)
                || namespace.equals(KubernetesConstants.RESERVED_NAMESPACE_KUBE_SYSTEM));
    }

    private boolean isNamespaceExists(String namespace) {
        for(Namespace ns: kubernetesClient.namespaces().list().getItems()) {
            if (ns.getMetadata().getName().equals(namespace)) {
                return true;
            }
        }
        return false;
    }
}
