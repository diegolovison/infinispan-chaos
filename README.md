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

## Metrics Server
```
minikube addons list | grep metrics-server
| metrics-server              | minikube | disabled     |
```
```
minikube addons enable metrics-server
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

When latency is high, probably you won't wait a lot of time.
The following argument allows you to change the iterations percent.
```bash
mvn verify -Dinfinispan-chaos.it_pct=0.1
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

# Bash Cheat Sheet
### Count file lines
```bash
wc -l file
```

### File size
```bash
du -sh file
```

# Troubleshooting

## Delete namespace from minikube
1. `kubectl delete all --all -n ispn-testing`
2. `kubectl delete namespace ispn-testing`
3. `kubectl get ns ispn-testing -o json > tmp.json`
4. Remove `kubernetes` from `finalizers`
```json
"finalizers": [
    "kubernetes"
]
```
3. Start proxy `kubectl proxy`
4. Execute the API call `curl -k -H "Content-Type: application/json" -X PUT --data-binary @tmp.json http://127.0.0.1:8001/api/v1/namespaces/ispn-testing/finalize`
5. Double check if the namespace was removed `kubectl get ns ispn-testing`