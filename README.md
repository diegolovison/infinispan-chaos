# Minikube
## Start Minikube
```bash
minikube start --driver=virtualbox --cpus 4 --memory "8192mb" --network-plugin=cni
```

## Install Infinispan Operator
https://operatorhub.io/operator/infinispan

## Install Chaos Mesh
https://chaos-mesh.org/docs/quick-start/

## Create namespace
```bash
kubectl create namespace ispn-testing
```

# Openshift

## Install Chaos Mesh
https://chaos-mesh.org/docs/quick-start/

## Create namespace
```bash
oc new-project ispn-testing
```

# Export the environment variables
```bash
export CHAOS_TESTING_ENVIRONMENT=openshift
export OPENSHIFT_URL=https://api.host:6443
export OPENSHIFT_TOKEN=sha256~mYtOkEn
```

# Run the test
```bash
mvn verify
```

# Changing default namespace
```bash
export CHAOS_TESTING_NAMESPACE=mynamespace
mvn verify
```
```java
ChaosTesting chaosTesting = new ChaosTesting();
chaosTesting.namespace("othernamespace");
```

# Set HotRod user and password
```bash
mvn verify -Dinfinispan.client.hotrod.auth_username=admin -Dinfinispan.client.hotrod.auth_password=password
```

# kubectl Cheat Sheet
### Copy from pod to locally
```bash
kubectl cp example-infinispan-0:/opt/infinispan/server/data example-infinispan-0
```