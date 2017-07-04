# TFG - Development of a chatbot for tourist recommendations  

Author: Jasmin Wellnitz  
Tutor: Bruno Baruque Zanón  
Department: Ingeniería Civil, Área de Lenguajes y Sistemas Informáticos  
Universidad de Burgos  

----
 Telegram Chatbot: @touristrecommenderbot
 
 The chatbot takes a bit to respond if not accessed for a while, this
 can be explained by the Platform as a service running behaviour.
 
 [Demonstration Video](Documentation/demonstration.rar)
----
## Description

This project deals with the development of a chatbot application that
is able to recommend tourist points of interest customized to the user's
preferences. The chatbot can be accessed using the instant messaging
app Telegram.

The project's main focus lies on the development of a server-side
architecture to the Telegram interface. In order to do so, a Java web
application is set up that uses the natural language processing platform
API.AI to parse the user input. The application is deployed to
the platform-as-a-service Heroku.

The recommendations given to the user are based on the information
the user shares with the chatbot and already existing data of similar
users. The applied recommendation algorithm combines the two most
important approaches of recommendation theory, content-based and collaborative filtering.

Furthermore, a geographic database is set up using PostGis, providing
the needed tourist information the recommendations are based on.
In the presented example of the application, geographic information of
Barcelona, Spain, is used and therefore limiting the provided recommendations to this city.