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

import io.swagger.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.c5.multitenancy.k8s.KubernetesTenancyProvider;
import org.wso2.c5.multitenancy.models.Tenant;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Handles tenants in a containerized environment. The service is available at.
 * http://localhost:8080/tenants
 */
@Api(value = "service", description = "Manage tenants in containerized environment")
@SwaggerDefinition(
        info = @Info(
                title = "Tenants Swagger Definition",
                version = "1.0",
                description = "Tenants service. Manages tenants in a containerized environment.",
                license = @License(
                        name = "Apache 2.0",
                        url = "http://www.apache.org/licenses/LICENSE-2.0"
                ),
                contact = @Contact(
                        name = "WSO2 Inc.",
                        email = "dev@wso2.org",
                        url = "http://wso2.com"
                )
        )
)
@Path("/tenants")
public class TenantService {

    private static final Logger log = LoggerFactory.getLogger(TenantService.class);

    private ITenancyProvider tenancyProvider;

    public TenantService() {
        this.tenancyProvider = new KubernetesTenancyProvider();
    }

    /**
     * Get list of all the available tenants.
     * http://localhost:8080/tenants
     *
     * @return Response
     */
    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(
            value = "Get all tenants",
            notes = "Returns all the available tenants",
            response = Tenant.class,
            responseContainer = "List")
    public Response getAllTenants() {
        return Response.ok()
                .entity(tenancyProvider.getTenants())
                .build();
    }

    /**
     * Get details of a particular tenant.
     * http://localhost:8080/tenants/tenant-a
     *
     * @param name Tenant name
     * @return Response
     */
    @GET
    @Path("/{name}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(
            value = "Get a tenant",
            notes = "Find and return a tenant by name")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Tenant found"),
            @ApiResponse(code = 404, message = "Tenant not found")
    })
    public Response getTenant(@ApiParam(value = "Tenant name", required = true) @PathParam("name") String name) {
        Tenant tenant = tenancyProvider.getTenant(name);
        if (tenant != null) {
            return Response.ok()
                    .entity(tenant)
                    .build();
        }
        return Response.status(Response.Status.NOT_FOUND)
                .build();
    }

    /**
     * Add a new tenant
     * curl -X POST -H "Content-Type: application/json" -d '{ name: "tenant-a" }' http://localhost:8080/tenants
     *
     * @param tenant Tenant object
     * @return Response
     */
    @POST
    @Path("/")
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(
            value = "Add a new tenant")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Tenant added"),
            @ApiResponse(code = 500, message = "Unable to add the tenant")
    })
    public Response addTenant(@ApiParam(value = "Tenant object", required = true) Tenant tenant) {
        if (tenancyProvider.createTenant(tenant)) {
            return Response.ok()
                    .build();
        }
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .build();
    }

    /**
     * Delete a tenant
     * curl -X DELETE http://localhost:8080/tenants/tenant-a
     *
     * @param name Tenant name
     * @return Response
     */
    @DELETE
    @Path("/{name}")
    @ApiOperation(
            value = "Delete a tenant",
            notes = "Delete a tenant identified by name")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Tenant deleted"),
            @ApiResponse(code = 404, message = "Tenant not found")
    })
    public Response delete(@ApiParam(value = "Tenant name", required = true) @PathParam("name") String name) {
        if (tenancyProvider.deleteTenant(name)) {
            return Response.ok()
                    .build();
        }
        return Response.status(Response.Status.NOT_FOUND)
                .build();
    }
}
