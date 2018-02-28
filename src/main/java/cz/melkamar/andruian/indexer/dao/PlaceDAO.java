package cz.melkamar.andruian.indexer.dao;

import cz.melkamar.andruian.indexer.exception.NotImplementedException;
import cz.melkamar.andruian.indexer.model.place.Place;

import java.util.List;

public class PlaceDAO {
    public void savePlace(Place place) {
        throw new NotImplementedException();
    }

    public void deletePlace(Place place) {
        throw new NotImplementedException();
    }

    public List<Place> getAllPlaces() {
        throw new NotImplementedException();
    }

    public List<Place> getPlacesOfClass(String classUri) {
        throw new NotImplementedException();
    }

    public List<Place> getPlacesAroundPoint(double latCoord, double longCoord, double radius) {
        throw new NotImplementedException();
    }

    public List<Place> getAllPlaces(String classUri, double latCoord, double longCoord, double radius) {
        throw new NotImplementedException();
    }
}
