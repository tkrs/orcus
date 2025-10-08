# Changelog

## [0.28.14](https://github.com/tkrs/orcus/compare/v0.28.13...v0.28.14) (2025-10-08)


### Miscellaneous Chores

* release 0.28.14 ([#942](https://github.com/tkrs/orcus/issues/942)) ([1a68f6b](https://github.com/tkrs/orcus/commit/1a68f6bc62fa4212ac030a2a6aabed056a1d5b31))

## [0.28.13](https://github.com/tkrs/orcus/compare/v0.28.3...v0.28.13) (2025-06-14)


### Miscellaneous Chores

* release 0.28.12 ([547b222](https://github.com/tkrs/orcus/commit/547b22294493765972afe5d5e80269895164ad9e))
* release 0.28.13 ([#910](https://github.com/tkrs/orcus/issues/910)) ([b6b3bcb](https://github.com/tkrs/orcus/commit/b6b3bcbed487f934fb2441e8f27f2ae35d3d6fa7))


### Build System

* **deps:** bump google-cloud-bigtable from 2.58.2 to 2.60.0 ([#905](https://github.com/tkrs/orcus/issues/905)) ([180c29d](https://github.com/tkrs/orcus/commit/180c29dcaa36f73412fce5c0d9e6892a813be1f7))

## [0.28.12](https://github.com/tkrs/orcus/compare/v0.28.11...v0.28.12) (2025-05-01)


### Miscellaneous Chores

* release 0.28.12 ([547b222](https://github.com/tkrs/orcus/commit/547b22294493765972afe5d5e80269895164ad9e))

## v0.16.2 (06/08/2018)
- #148 Upgraded HBase client
---

## v0.13.0 (27/03/2018)
Support for asynchronous APIs and remove synchronous APIs.

https://github.com/tkrs/orcus/compare/v0.12.0...v0.13.0
---

## v0.12.0 (27/03/2018)
## Bugfix
- Fix to avoid a null value #58 

## Improvement
- Return empty Map when its result cells is empty #71 

https://github.com/tkrs/orcus/compare/v0.11.0...v0.12.0
---

## v0.11.0 (27/03/2018)
- Adopt iota #56 
- Update to cats 1.0.0 #47 

https://github.com/tkrs/orcus/compare/v0.10.2...v0.11.0
---

## v0.10.2 (26/12/2017)
- Add Decoder[Map[String, A]] #52
---

## v0.10.1 (25/12/2017)
- Fixed decoding Failure in ValueCodec #50
---

## v0.10.0 (25/12/2017)
- Make to creation the nullable column #49
---

## v0.9.0 (25/12/2017)
- Add timestamp to PutEncoder/PutFamilyEncoder arguments #36
- Replace Encoder result with Put from Option[Put] #37
- Fix incorrect handling of the Option[A] instance #41 
- Fix problems that do not catch NPE #42 
- 
---

## v0.8.0 (21/12/2017)
- Add PutEncoder of a Map #34 
---

## v0.7.0 (21/12/2017)
- Derive `Put` from`A` automatically #33
---

## v0.6.0 (21/12/2017)
- More FP friendly methods: #29 and #30
- Refactor row builders with Reader Monad #31 

---

## v0.5.0 (21/12/2017)
- Add FP friendly methods: #28
---

## v0.4.2 (21/12/2017)
Fix an incorrect decoding handling. #22, #26 
---

## v0.4.1 (13/12/2017)
Fixes release for v0.4.0
---

## v0.1.0 (13/12/2017)
First release!
---

## v0.2.0 (13/12/2017)
Supports following operations:

- Result#getRow
- Result#rawCells
- Result#getColumnCells
- Result#getColumnLatestCell
- Result#getFamilyMap

---

## v0.3.0 (13/12/2017)
`Codec`'s type-classes derivation will be able to automatically with Shapeless.
---

## v0.4.0 (13/12/2017)
**DEPRECATED** This release was failed...
