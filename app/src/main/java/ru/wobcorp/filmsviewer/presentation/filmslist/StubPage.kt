package ru.wobcorp.filmsviewer.presentation.filmslist

import ru.wobcorp.filmsviewer.domain.FilmModel
import ru.wobcorp.filmsviewer.utils.pagination.Page

val initialPage = Page(listOf<FilmModel>(
    FilmModel(id = 1, imageLink = "/aKx1ARwG55zZ0GpRvU2WrGrCG9o.jpg",  title = "Mulan", overview = ""),
    FilmModel(id = 2, imageLink = "/9Rj8l6gElLpRL7Kj17iZhrT5Zuw.jpg", title =  "Santana", overview = ""),
    FilmModel(id = 3, imageLink = "/ugZW8ocsrfgI95pnQ7wrmKDxIe.jpg", title =  "Hard Kill", overview = ""),
    FilmModel(id = 4, imageLink = "/sMO1v5TUf8GOJHbJieDXsgWT2Ud.jpg",  title = "Unknown Origins", overview = ""),
    FilmModel(id = 5, imageLink = "/aKx1ARwG55zZ0GpRvU2WrGrCG9o.jpg", title =  "Mulan", overview = ""),
    FilmModel(id = 6, imageLink = "/kPzcvxBwt7kEISB9O4jJEuBn72t.jpg",  title = "We Bare Bears", overview = ""),
    FilmModel(id = 7, imageLink = "/6CoRTJTmijhBLJTUNoVSUNxZMEI.jpg",  title = "Money Plane", overview = ""),
    FilmModel(id = 8, imageLink = "/uOw5JD8IlD546feZ6oxbIjvN66P.jpg", title =  "Rogue", overview = ""),
    FilmModel(id = 9, imageLink = "/sy6DvAu72kjoseZEjocnm2ZZ09i.jpg",  title = "Peninsula", overview = ""),
    FilmModel(id = 10, imageLink = "/o1WvNhoackad1QiAGRgjJCQ1Trj.jpg", title =  "The 2nd", overview = ""),
    FilmModel(id = 11, imageLink = "/TnOeov4w0sTtV2gqICqIxVi74V.jpg", title =  "Project Power", overview = ""),
    FilmModel(id = 12, imageLink = "/kiX7UYfOpYrMFSAGbI6j1pFkLzQ.jpg", title =  "After We Collided", overview = ""),
    FilmModel(id = 13, imageLink = "/n6hptKS7Y0ZjkYwbqKOK3jz9XAC.jpg",  title = "Phineas and Ferb  The Movie Candace Against the Universe", overview = ""),
    FilmModel(id = 14, imageLink = "/i4kPwXPlM1iy8Jf3S1uuLuwqQAV.jpg", title =  "One Night in Bangkok", overview = ""),
    FilmModel(id = 15, imageLink = "/uGhQ2ZGBpzCj6wC5jUrybsZuPTI.jpg", title =  "Ava", overview = ""),
    FilmModel(id = 16, imageLink = "/eDnHgozW8vfOaLHzfpHluf1GZCW.jpg", title =  "Archive", overview = ""),
    FilmModel(id = 17, imageLink = "/lv3RonWge4GlC9ymNzC0oWpFCfv.jpg",  title = "Double World", overview = ""),
    FilmModel(id = 18, imageLink = "/hTFs6T2k0kdQHFcC4gisT0UtNX8.jpg",  title = "Centigrade", overview = ""),
    FilmModel(id = 19, imageLink = "/sDi6wKgECUjDug2gn4uODSqZ3yC.jpg", title =  "The Crimes That Bind", overview =""),
    FilmModel(id = 20, imageLink = "/aKx1ARwG55zZ0GpRvU2WrGrCG9o.jpg",  title = "Mulan", overview = "")
), 1)

val emptyPage = Page<FilmModel>(emptyList(), 0)