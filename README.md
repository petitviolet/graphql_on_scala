# GraphQL server sample with sangria and Akka-HTTP

Sample implementation for Scala-kansai 2018.

## run

```
sbt main/run
```

and then open `localhost:8080` in browser.

## query example
 
```
query MyQuery {
  user {
    all {
      id
    }
    by_email(email: "hello@example.com") {
      id
      name
      email
    }
  }
  todo {
    all {
      id
    }
    search(user_id: "hello") {
      id
      title
      description
      user {
        id
        name
      }
    }
  }
}
```

## mutation example

```
mutation UpdateTodo {
  todo {
    update(user_id: "hello", id: "todo-1", title: "updated", description: "updated-description") {
      title
      description
    }
  }
}
```

## build

```shell-session
sbt 'project main' 'docker:publishLocal'
```

### run with curl


```shell-session
$ curl 'localhost:8080/graphql' -H"Content-Type: application/json" -XPOST -d'{"mutation": "mutation Login { user { login(email: \"hoge@example.com\", password: \"password\") } }"}'
{"data":{"user":{"login":"e541c76d-efcb-4ab9-834c-a6688687df11"}}}%

$ curl 'localhost:8080/graphql' -H"Content-Type: application/json" -XPOST -d'{"mutation": "mutation UpdateUser { user { update(id: \"1\", name: \"updated2\") { id  name }  }}"}' -H"X-Token: 99d4f9b4-a092-4caf-8bc1-40cf716f5760"
{"data":{"user":{"update":{"id":"1","name":"updated2"}}}}% 
```
