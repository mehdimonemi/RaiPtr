 CASE
                    WHEN f.F2201 = s.f4612 THEN 0/*reach destination*/
                /*some stations are actually as one*/
                    WHEN s.f4612 = 381
                        AND f.F2201 = 408 THEN 0
                    WHEN s.f4612 = 384
                        AND f.F2201 = 411 THEN 0
                    WHEN s.f4612 = 414
                        AND (
                                     f.F2201 = 415
                                 OR f.F2201 = 416
                                 OR f.F2201 = 769
                                 OR f.F2201 = 768
                                 OR f.F2201 = 767
                                 OR f.F2201 = 642
                             ) THEN 0
                    WHEN s.f4612 = 352
                        AND f.F2201 = 605 THEN 0
                    WHEN (s.f4612 = 499 OR s.f4612 = 708)
                        AND f.F2201 = 604 THEN 0
                    WHEN s.f4612 = 602
                        AND (f.F2201 = 750 OR f.F2201 = 603) THEN 0
                    WHEN s.f4612 = 116
                        AND f.F2201 = 465 THEN 0
                    WHEN s.f4612 = 424
                        AND f.F2201 = 425 THEN 0
                    WHEN s.f4612 = 625
                        AND f.F2201 = 636 THEN 0
                    WHEN s.f4612 = 165
                        AND f.F2201 = 474 THEN 0
                    WHEN s.f4612 = 273
                        AND f.F2201 = 388 THEN 0
                    WHEN s.f4612 = 298
                        AND f.F2201 = 426 THEN 0
                    WHEN s.f4612 = 393
                        AND f.F2201 = 392 THEN 0
                    WHEN s.f4612 = 341
                        AND f.F2201 = 391 THEN 0
                    WHEN s.f4612 = 363
                        AND f.F2201 = 390 THEN 0
                    WHEN s.f4612 = 352
                        AND f.F2201 = 605 THEN 0
                    WHEN s.f4612 = 459
                        AND (f.F2201 = 390 OR f.F2201 = 605) THEN 0
                    WHEN s.f4612 = 585
                        AND f.F2201 = 635 THEN 0
                    WHEN s.f4612 = 586
                        AND f.F2201 = 634 THEN 0
                    WHEN s.f4612 = 580
                        AND f.F2201 = 586 THEN 0
                    WHEN f.F2201 = F15.F1506 THEN 1/*reach train destination*/
                    WHEN f.F2201 = s.f4610 THEN 2/*reach detach Station*/
                    ELSE 3/*Moving*/
                    END