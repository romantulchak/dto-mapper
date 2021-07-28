## Information
This library allows you to convert your entities to a selected DTO class in a very easy way.

## How to use:
1. Copy repository locally

2. Create Jar (Without any dependencies) or move packeges to your project

3. Create configuration file and paste code below 
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
4. Autowire EntityMapperInvoker into your service
```java
@Autowired
private EntityMapperInvoker<YourDTO, YourEntity> entityMapperInvoker;
```
5. Mark the fields in the DTO that you want to be mapped using 
```java  
@MapToDTO(mapClass = {View.TripView.class, View.SeatTripView.class})
```
mapClass is needed to avoid loops that work like JsonView
 
6. And in your method just add next line
```java
return entityMapperInvoker.entityToDTO(entity object, DTO.class, mapClass from the annotation); 
```


 
