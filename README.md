## SABD:SchoolBusAnalyzer

SchoolBusAnalyzer è un sistema per l’analisi dei dati relativi ai ritardi degli autobus scolastici della città di New York.
L’architettura è costituita da:

- due possibili scelte per effettuare l'ingestion dei dati, rappresentate rispettivamente da un producer di Apache Pulsar e un producer di Apache Kafka;
- due possibili scelte per effettuare il processamento dei dati, rappresentate rispettivamente da un consumer di Apache Flink e un consumer di Apache Kafka che utilizza la libreria Kafka Streams;
- un nodo per il coordinamento dei nodi Kafka, ospitante Apache ZooKeeper;
- un nodo broker per la comunicazione publish-subscribe, ospitante Apache Kafka;
- un nodo per l'accesso alla dashboard di Pulsar.

### Build dell’architettura
Per istanziare il sistema è necessario:
- aver istallato il supporto docker in locale
- eseguire il comando ```$docker-compose up -- build ``` 
per effettuare la pull e la build delle immagini.

### Producers
Per avviare la simulazione del sistema è necessario avviare uno dei producer a scelta:

- Avvio di Apache Pulsar

Per avviare la simulazione dell'invio del dataset è necessario:
1. spostarsi sul nodo del producer di Pulsar eseguendo: 
       ``` $docker exec -it pulsar-node bash```
2. spostarsi all'interno della cartella principale:
    ``` $cd ..``` 
    ``` $cd pulsar-jar/``` 
3. eseguire il comando per la submit del dataset:
    ``` $sh submit-dataset.sh -f bus-breakdown-and-delays.csv -s <speed_factor> -t <topic_name>```
dove:
 ```<speed_factor>``` è il fattore di accelerazione temporale desiderato per la simulazione;
 ``` <topic_name>``` è il nome del topic corrispondente ai dati della query che si vuole processare (dataQuery1, dataQuery2, dataQuery3);
 
 - Avvio di Apache Kafka
 
 Per avviare la simulazione dell'invio del dataset è necessario:
 1. spostarsi sul nodo del producer di Kafka eseguendo: 
        ``` $docker exec -it kafka-producer bash```
 2. spostarsi all'interno della cartella principale:
    ``` $cd producer/``` 
 3. eseguire il comando per la submit del dataset:
     ``` $sh submit-dataset.sh -f bus-breakdown-and-delays.csv -s <speed_factor>  -t <topic_name> ```
 dove:
  ```<speed_factor>``` è il fattore di accelerazione temporale desiderato per la simulazione;
  ```<topic_name>``` è il nome del topic corrispondente ai dati della query che si vuole processare (dataQuery1, dataQuery2, dataQuery3);

  
### Consumers
Per invocare l’esecuzione delle varie query è necessario avviare uno dei consumatori a scelta:

 - Avvio dei Apache Flink
 
 Per avviare la computazione delle query è necessario:
 1. spostarsi sul nodo del consumer (master) di Flink eseguendo: 
        ``` $docker exec -it flink-jobmanager bash```
 2. creare le cartelle "results-kafka" e "results-pulsar" all'interno della directory ed eventualmente cambiare i permessi di scrittura (anche a partire dal volume docker-compose/flink-jar)
 3. eseguire il comando per la submit della query:
     ``` $sh submit-query.sh -c <connector_type> -q <num_query> [-v <query2_type>] -t <topic_name>```
 dove:
  ```<connector_type>``` è il nome del tipo di producer a cui ci si vuole connettere (pulsar, kafka);
  ```<num_query>: [1,2,3]``` corrisponde al numero della query che si vuole eseguire (1,2,3)
  ```<query2_type>``` corrisponde al tipo di implementazione che si vuole eseguire per questa specifica query:
    - aggregate è la versione che utilizza esclusivamente aggregatori
    - split è la versione che utilizza sia aggregatori che una co-group
  ``` <topic_name>``` è il nome del topic corrispondente ai dati della query che si vuole processare (dataQuery1, dataQuery2, dataQuery3);
  
  - Avvio di Apache Kafka+KafkaStreams
  
  Per avviare la computazione delle query è necessario:
  1. spostarsi sul nodo del consumer di Kafka eseguendo: 
         ``` $docker exec -it kafka-consumer bash``` 
  2. spostarsi all'interno della cartella principale:
       ``` $cd consumer/``` 
  3. eseguire il comando per la submit della query2:
      ``` $sh submit-query.sh -q <num_query> ```
  dove:
  ```<num_query>: [1,2]``` corrisponde al tipo di finestra temporale che si vuole eseguire della Query2 (1=day, 2=week).
 
 Tutte le possibili configurazioni delle run degli .sh sono riportate nel file RUN.md.
 
 Per accedere alle dashboard collegarsi a:
 - dashboard di Pulsar:  http://localhost:80
 - dashboard di Flink:  http://localhost:8081

 
 
