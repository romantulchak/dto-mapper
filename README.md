## Information
This library allows you to convert your entities to a selected DTO class in a very easy way.

## How to use:
1. Download last version of Jar from Release page

2. Create configuration file and paste code below 
```java
@Configuration
@ComponentScan("com.mapperDTO")
public class ApplicationConfig {
    @Bean
    public EntityMapper newEntityMapper(){
        return new EntityMapper();
    }
    @Bean
    public EntityMapperInvoker<Object, Object> newEntityMapperInvoker(){
        return new EntityMapperInvoker<>();
    }
}
```
3. Autowire EntityMapperInvoker into your service
```java
@Autowired
private EntityMapperInvoker<YourDTO, YourEntity> entityMapperInvoker;
```
4. Mark the fields in the DTO that you want to be mapped using 
```java  
@MapToDTO(mapClass = {View.TripView.class, View.SeatTripView.class})
private long id;
```
mapClass is needed to avoid loops that work like JsonView
 
5. Mark the DTO class 
```java  
@DTO
public class CityDTO {
//your code
}
```

6. And in your method just add next line
```java
return entityMapperInvoker.entityToDTO(entity object, DTO.class, mapClass from the annotation); 
```





**Use the "associatedField" property in the ```@MapToDTO``` annotation if your variable in the DTO has a different name than in the Entity**
 
