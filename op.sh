#!/bin/sh

main() {
    get_passed_arguments $@

    # Download images and setup network
    if [ -n "$compose" ] && [ "$compose" = "up" ]; then
        docker-compose -f docker-compose.yml up -d
        exit 0
    fi;

    # Destroy everything
    if [ -n "$compose" ] && [ "$compose" = "down" ]; then
        docker-compose -f docker-compose.yml down
        exit 0
    fi;

    # Start containers that are created already
    if [ -n "$compose" ] && [ "$compose" = "start" ]; then
        docker-compose -f docker-compose.yml start
        exit 0
    fi;

    # Stop containers and do not destroy them
    if [ -n "$compose" ] && [ "$compose" = "stop" ]; then
        docker-compose -f docker-compose.yml stop
        exit 0
    fi;

    # Just list all topics
    if [ -n "$topic" ] && [ "$topic" = "list" ]; then
        docker exec -it broker /bin/kafka-topics --zookeeper 172.25.0.11:2181 --list
        exit 0
    fi;

    # Create topic if topic argument value is something else than list
    if [ -n "$topic" ]; then
        docker exec -it broker /bin/kafka-topics --create --topic $topic --bootstrap-server 172.25.0.12:9092
        exit 0
    fi;

    # Just list all topics
    if [ -n "$topic" ] && [ "$topic" = "list" ]; then
        docker exec -it broker /bin/kafka-topics --zookeeper 172.25.0.11:2181 --list
        exit 0
    fi;

    # Subscribe to topic
    if [ -n "$subscribe" ]; then
        docker exec -it broker /bin/kafka-console-consumer --topic $subscribe --bootstrap-server 172.25.0.12:9092
        exit 0
    fi;

    # Run test producer on topic
    if [ -n "$producer" ]; then
        docker exec -it broker /bin/kafka-console-producer --topic $producer --bootstrap-server 172.25.0.12:9092
        exit 0
    fi;
}

get_passed_arguments() {
    for arg in "$@"
    do
        key=$(echo $arg | cut -f1 -d=)
        value=$(echo $arg | cut -f2 -d=)

        case "$key" in
            compose) compose=${value} ;;
            topic) topic=${value} ;;
            subscribe) subscribe=${value} ;;
            producer) producer=${value} ;;
            *) "Wrong argument passed."
            return 9;;
        esac
    done
}

main $@