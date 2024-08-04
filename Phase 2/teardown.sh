lsof -ti :8080
sleep 5
lsof -ti :8081
sleep 5
lsof -ti :8082
sleep 5

kubectl delete deployment --all
kubectl delete svc --all

eval $(minikube docker-env)
printf "please wait 30 seconds..."
sleep 30
docker image rmi manas-user-service
docker image rmi manas-wallet-service
docker image rmi manas-booking-service
docker image rmi manas-h2db-service
