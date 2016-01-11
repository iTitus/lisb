# lisb

Write B in Clojure. Interact with the ProB constraint solver.


## Examples

See src/lisb/examples


### Known Bugs



### To Do

- [ ] painless conversion of Clojure data structures into B data
- [ ] painless conversion from B data into Clojure data structures
- [ ] special nodes
    - [x] if-then-else
    - [x] let (B)
    - [ ] choose
- [ ] strings containing B code
    - [X] parsing of B strings
    - [ ] matching of variables
- [ ] exhaustive implementation of B operators
- [ ] more examples
- [ ] support for external functions
- [X] make semantics or not= and range clearer / clojure-y
- [ ] use B machines as contexts
- [ ] something like (+ 1) should result in 1 when translated

## License

Copyright © 2015 FIXME

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
