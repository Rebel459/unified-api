# Item predicates

Component modifiers and block conversions accept one `predicate` using
`ExtensibleCodecs.ITEM_PREDICATE_TYPES`. Component modifiers test the item's
default stack; block conversions test the stack used on the block.

```json
{
  "predicate": {
    "type": "unified:all_of",
    "predicates": [
      { "type": "unified:items", "items": "#minecraft:swords" },
      {
        "type": "unified:not",
        "predicate": {
          "type": "unified:items",
          "items": ["minecraft:wooden_sword"]
        }
      }
    ]
  },
  "components": { "minecraft:max_stack_size": 1 }
}
```

- `unified:items`: an `items` holder set (a tag string or list of item IDs).
  An empty list matches nothing.
- `unified:components`: a `components` map. Every listed value must equal the
  stack's component value. An empty map matches any stack.
- `unified:always` and `unified:never`: no fields; also accept the compact form
  `"predicate": "unified:always"`.
- `unified:all_of` and `unified:any_of`: a `predicates` list, evaluated with
  short-circuiting. Empty lists match everything and nothing, respectively.
- `unified:not`: one nested `predicate`.

The five boolean operations are provided by `ExtensiblePredicate<P>`, where `P`
is the complete boolean function type. Item and block-state predicates,
`StatePredicate`, and both `StateArgumentPredicate` categories share them.

Use `ExtensiblePredicate.predicate("type")` for `Predicate<T>` or
`ExtensiblePredicate.biPredicate("type")` for `BiPredicate<A, B>`. Other boolean
function interfaces need only an adapter forwarding their arguments:

```java
ExtensiblePredicate<BlockBehaviour.StatePredicate> predicates = new ExtensiblePredicate<>(
        "type",
        evaluation -> (state, level, pos) ->
                evaluation.evaluate(predicate -> predicate.test(state, level, pos))
);
```

Each nested predicate receives the original arguments. All/any operations retain
short-circuit evaluation. Each category resolves nested types through its own
registry.

Mods can register additional item predicates using the existing registration
pattern before resource decoding:

```java
ExtensibleCodecs.ITEM_PREDICATE_TYPES.register(
        Identifier.fromNamespaceAndPath("example", "count_at_least"),
        Codec.INT.fieldOf("count"),
        count -> stack -> stack.getCount() >= count
);
```

Registered predicates can be nested in the shared boolean operations. The old
component-modifier `targets` list and untyped item/component predicate objects
have been replaced outright.
