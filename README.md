# energiatili

Fetch information about electricity consumption from [Energiatili](https://www.energiatili.fi/)

## Installation

You need clojure. Best to install it directly from
https://clojure.org/guides/getting_started . OS packages are usually
out of date or otherwise weird. 

I use Sean Corfield's
[deps.edn](https://github.com/seancorfield/dot-clojure). If you have
that you can create an executable with 
`clojure -Spom` and 
`clojure -X:uberjar :jar energiatili.jar :main-class energiatili.core` 

## Configuration

Edit energiatili-fetch to include your credentials and whether you
want all data or just the latest (default). Also edit where your
influx db is located. 

## Inspiration

I was inspired by <https://github.com/Tarlab/energiatili> and wanted
to see what it would look like when written in Clojure. 
