minikube start 
eval $(minikube docker-env)

lsof -ti :8080
sleep 5
lsof -ti :8081
sleep 5
lsof -ti :8082
sleep 5

cd USER
docker build -t manas-user-service .
kubectl apply -f user-deployment.yaml
kubectl apply -f user-service.yaml
cd ..

cd WALLET
docker build -t manas-wallet-service .
kubectl apply -f wallet-deployment.yaml
kubectl apply -f wallet-service.yaml
cd ..

cd H2DB
docker build -t manas-h2db-service .
kubectl apply -f h2db-deployment.yaml
kubectl apply -f h2db-service.yaml
cd ..

cd BOOKING
docker build -t manas-booking-service .
kubectl apply -f booking-deployment.yaml
kubectl apply -f booking-service.yaml
cd ..

printf "wait 15 seconds..."
sleep 15

kubectl port-forward service/manas-user-service 8080:8080 &
kubectl port-forward service/manas-wallet-service 8082:8080 &
kubectl port-forward service/manas-booking-service 8081:8080 &


