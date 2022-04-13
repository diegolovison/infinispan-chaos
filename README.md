# Start Minikube
```bash
minikube start --driver=virtualbox --cpus 4 --memory "8192mb" --network-plugin=cni
```

# Install Infinispan Operator
https://operatorhub.io/operator/infinispan

# Install Chaos Mesh
https://chaos-mesh.org/docs/quick-start/

# Create namespace
```bash
kubectl create namespace ispn-testing
```

# Deploy Infinispan
```yaml
apiVersion: infinispan.org/v1
kind: Infinispan
metadata:
  name: example-infinispan
spec:
  replicas: 3
```

# Run the test
```bash
mvn verify
```

# Run the test on Openshift
```bash
export CHAOS_TESTING_ENVIRONMENT=openshift
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