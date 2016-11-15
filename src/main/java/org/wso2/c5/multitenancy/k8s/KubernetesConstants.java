package org.wso2.c5.multitenancy.k8s;

/**
 * Kubernetes constants
 */
public class KubernetesConstants {

    public static final String DEFAULT_KUBERNETES_MASTER_IP = "172.17.8.101";
    public static final String DEFAULT_KUBERNETES_MASTER_PORT = "8080";

    public static final String KUBERNETES_MASTER_IP_ENV = "KUBERNETES_MASTER_IP";
    public static final String KUBERNETES_MASTER_PORT_ENV = "KUBERNETES_MASTER_PORT";

    public static final String RESERVED_NAMESPACE_DEFAULT = "default";
    public static final String RESERVED_NAMESPACE_KUBE_SYSTEM = "kube-system";
}
