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

package org.wso2.c5.multitenancy;

import org.wso2.c5.multitenancy.k8s.KubernetesTenancyProvider;
import org.wso2.c5.multitenancy.models.Tenant;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * This is the Microservice resource class.
 * See <a href="https://github.com/wso2/msf4j#getting-started">https://github.com/wso2/msf4j#getting-started</a>
 * for the usage of annotations.
 *
 * @since 1.0-SNAPSHOT
 */
@Path("/service")
public class TenantService {

    private ITenancyProvider tenancyProvider;

    public TenantService() {
        this.tenancyProvider = new KubernetesTenancyProvider();
    }

    @GET
    @Path("/tenants")
    @Produces(MediaType.APPLICATION_JSON)
    public Response list() {
        return Response.ok().entity(tenancyProvider.getTenants()).build();
    }

    @GET
    @Path("/tenant/{name}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response get(@PathParam("name") String name) {
        Tenant tenant = tenancyProvider.getTenant(name);
        if (tenant != null) {
            return Response.ok().entity(tenant).build();
        }
        return Response.status(Response.Status.NOT_FOUND).build();
    }

    @POST
    @Path("/tenant")
    public Response post(@FormParam("name") String name) {
        Tenant tenant = new Tenant(name);
        if (tenancyProvider.createTenant(tenant)) {
            return Response.ok().build();
        }
        return Response.status(Response.Status.NOT_FOUND).build();
    }

    @DELETE
    @Path("/tenant/{name}")
    public Response delete(@PathParam("name") String name) {
        if (tenancyProvider.deleteTenant(name)) {
            return Response.ok().build();
        }
        return Response.status(Response.Status.NOT_FOUND).build();
    }
}
