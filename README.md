# Start Minikube
```bash
minikube start --driver=virtualbox --cpus 4 --memory "8192mb" --network-plugin=cni
```

# Install Infinispan Operator
https://operatorhub.io/operator/infinispan

# Install Chaos Mesh
https://chaos-mesh.org/docs/quick-start/

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

# kubectl Cheat Sheet
### Copy from pod to locally
```bash
kubectl cp example-infinispan-0:/opt/infinispan/server/data example-infinispan-0
```