# Retrospector

An experimental star viewer that allows you to step through the galactic starfield in a hyper abstract view. Information about stars is aggregated and encoded into the placement and color of the grid. Moving in 3 dimensions and viewing in 2 dimensions allows you to maintain an objective macro view so you can take the all that pixel glory in!

![Alt text](/../master/resources/public/images/galaxy_output.png?raw=true "The Milky Way on the X axis")

## Usage

### From a server

```
lein deps
lein cljsbuild once
lein run

# Go to http://127.0.0.1:9000/app
# Press the up arrow to slice the galaxy!
```

### From the Repl

```
lein deps
lein repl

# In the repl
>> (in-ns 'retrospector.server)
>> (restart-server!)

# In your browser go to http://127.0.0.1:9000/app
>> (in-ns 'retrospector.app)
>> (reset-app!)

# Load a slice of the galaxy!
>> (make-request "x" 1000 1000)
```

## License

Copyright © 2014 Alex Kehayias

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
