
# Description

This is an example app that uses a Clojure backend and Clojurescript frontend.


__The template includes:__  
- Server with handler  
- CLJS app  


# Development

__How to start CLJ server:__

```bash
lein run
```



__How to start CLJS:__

```bash
lein figwheel
```



__How to start CLJS with in-editor REPLing__

```clojure
(use 'figwheel-sidecar.repl-api)
(start-figwheel! *Optionally insert profile name*)
(cljs-repl *Optionally insert profile name*)
```

# Plan - Session 1

1. Create new project file
2. Include relevant libraries
3. Write an index.html
4. Write CLJS basic app

5. Setup CLJS build

6. Write re-frame app
   - DB
   - Events
   - Core/main
   - Views
   - Subs

# To Do:  
- Practice creating project file  
- Practice flow from 1-4  
- Come up with 'extra credit' exercises  


*** Must have NPM installed to manage JS dependencies.

On Ubuntu::
```
sudo apt-get update
sudo apt-get install npm
```
