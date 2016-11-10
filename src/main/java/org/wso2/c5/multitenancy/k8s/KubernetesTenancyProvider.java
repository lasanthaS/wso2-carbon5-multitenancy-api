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
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.wso2.c5.multitenancy.ITenancyProvider;
import org.wso2.c5.multitenancy.models.Tenant;

import java.util.ArrayList;
import java.util.List;

public class KubernetesTenancyProvider implements ITenancyProvider {

    public static final String KUBERNETES_ENDPOINT_URL = "http://172.17.8.101:8080";

    private KubernetesClient kubernetesClient;

    public KubernetesTenancyProvider() {
        this.kubernetesClient = new DefaultKubernetesClient(KUBERNETES_ENDPOINT_URL);
    }

    public KubernetesTenancyProvider(String endpoint) {
        kubernetesClient = new DefaultKubernetesClient(endpoint);
    }

    @Override
    public Tenant[] getTenants() {
        List<Tenant> tenants = new ArrayList<>();
        for (Namespace ns : kubernetesClient.namespaces().list().getItems()) {
            tenants.add(new Tenant(ns.getMetadata().getName()));
        }
        return tenants.toArray(new Tenant[tenants.size()]);
    }

    @Override
    public Tenant getTenant(String name) {
        for (Namespace ns : kubernetesClient.namespaces().list().getItems()) {
            if (ns.getMetadata().getName().equals(name)) {
                return new Tenant(ns.getMetadata().getName());
            }
        }
        return null;
    }

    @Override
    public boolean createTenant(Tenant tenant) {
        Namespace ns = new Namespace();
        ns.setApiVersion(kubernetesClient.getApiVersion());
        ns.setMetadata(new ObjectMeta());
        ns.getMetadata().setName(tenant.getName());
        kubernetesClient.namespaces().create(ns);
        return true;
    }

    @Override
    public boolean deleteTenant(String name) {
        for (Namespace ns : kubernetesClient.namespaces().list().getItems()) {
            if (ns.getMetadata().getName().equals(name)) {
                kubernetesClient.namespaces().delete(ns);
                return true;
            }
        }
        return false;
    }
}
