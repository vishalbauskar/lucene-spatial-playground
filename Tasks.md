# Testing #
  * ~~Test CircleImpl thoroughly, especially intersection~~
  * ~~Port old geohash SOLR-2155 extensive test~~
  * ~~Test sorting~~
  * Test indexing shapes with area

# Performance / Efficiency #
In single valued field case, RecursivePrefix... should use a more efficient cache representation for sorting

# Framework Rework/Refactor #
  * ~~SpatialContext should not be a singleton; tie to a field.~~
  * ~~SpatialContext (with units, calculator, and world bounds) and the prefix trie impl, should be configurable in a Solr field type.~~
  * ~~Make SpatialPrefixTree impl configurable via a factory, and use it from Solr field type.~~
  * distPrec of query should default to that configured on the field.  And add localParams lookup?

# Features #
  * ~~Use needScore local-param for use of Filter vs. ValueSource~~
    * Demonstrate score-sorted order in README, demo app
  * Port & hack geodist() query parser for compatibility with LSP
  * **Benchmarking!**
  * ~~Range query [... TO ...] input method.~~

# Misc #
  * ~~Investigate how R-Trees differ from the RecursivePrefixFilter.~~ -- not same!
    * Find prior-art for Trie implementation of spatial (point, and shape/area too).
  * Are there things we need to do to be more compatible with [OGC](http://www.opengeospatial.org/standards)?
    * RectangleImpl constructor args order?
    * IntersectCase enum naming
    * Spatial operations naming
    * DistanceCalculator contains context instead of radius?
    * interpretation of "d" in Circle; degrees or distance?



---

# Not important for 1st Release #
  * A CircleImpl based on cartesian 2d model with dateline wrap, no pole.  Some people may want a circle as projected on mercator, not a true great-circle-distance circle.


## Extras / Demo Related ##
  * Implement proper polygon world-wrap & pole-wrap. Look elsewhere for ideas.