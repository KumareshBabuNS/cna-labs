cf cs p-mysql 100mb-dev reservation-db
cf cs p-service-registry standard service-registry
cf cs p-circuit-breaker-dashboard standard circuit-breaker-dashboard
cf cs -c '{"git":{"uri":"https://github.com/dwong-pivotal/cloud-native-workshop-config.git"}}' p-config-server standard config-service

