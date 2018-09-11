# Supported mutators

Stryker4s supports a variety of mutators, which are listed below. Do you have a suggestion for a (new) mutator? Feel free to create an [issue](https://github.com/stryker-mutator/stryker4s/issues)!

An always up-to-date reference is also available in the [MutantMatcher source](../core/src/main/scala/stryker4s/mutants/findmutants/MutantMatcher.scala).

## Binary operators

| Original | Mutated |
| --- | --- |
| `>=` | `>`, `<`, `==` |
| `>` | `<=`, `<`, `==` |
| `<=` | `<`, `>=`, `==` |
| `<` | `<=`, `>`, `==` |
| `==` | `!=` |
| `!=` | `==` |

## Boolean substitutions

| Original | Mutated |
| --- | --- |
| `true` | `false` |
| `false` | `true` |

## Logical operators

| Original | Mutated |
| --- | --- |
| `&&` | `||` |
| `||` | `&&` |

## String mutators

| Original | Mutated |
| --- | --- |
| `"foo"` (non-empty string) | `""` (empty string) |
| `""` (empty string) | `"Stryker was here!"` |
| `s"foo ${bar}"` (string interpolation) | `s""` <sup>1</sup> |

<sup>1: Only works with string interpolation and not others (like [Scalameta quasiquotes](https://scalameta.org/tutorial/#q%22Quasiquotes%22))to avoid compile errors</sup>

## Method mutators

| Original | Mutated |
| --- | --- |
| `a.filter(b)` | `a.filterNot(b)` |
| `a.filterNot(b)` | `a.filter(b)` |
| `a.exists(b)` | `a.forAll(b)` <sup>2</sup>|
| `a.forAll(b)` | `a.exists(b)` |
| `a.isEmpty` | `a.nonEmpty` |
| `a.nonEmpty` | `a.isEmpty` |
| `a.indexOf` | `a.lastIndexOf(b)` <sup>2</sup> |
| `a.lastIndexOf(b)` | `a.indexOf(b)` |
| `a.max` | `a.min` |
| `a.min` | `a.max` |

<sup>2: This can cause some false positives with unique lists, such as sets</sup>
