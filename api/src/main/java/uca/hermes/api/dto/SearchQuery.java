package uca.hermes.api.dto;

import lombok.*;
import uca.hermes.api.dao.Place;
import uca.hermes.api.util.StringUtilities;

@AllArgsConstructor
@Builder
@Getter
@Setter
public class SearchQuery {
    public String searchString;
    public String[] tagsList;

    public SearchQuery() {
        searchString = "";
        tagsList = new String[0];
    }

    public void normalize() {
        if (StringUtilities.isBlankOrNull(searchString)) {
            searchString = "";
        }
        searchString = searchString.toLowerCase();

        if (tagsList == null) {
            tagsList = new String[0];
        } else {
            for(int i = 0; i < tagsList.length; i++) {
                tagsList[i] = tagsList[i].toLowerCase();
            }
        }
    }

    public boolean checkPlace(Place place) {
        String name = (place.getName() == null) ? "" : place.getName().toLowerCase();
        String description = (place.getDescription() == null) ? "" : place.getDescription().toLowerCase();
        if (!name.contains(searchString) && !description.contains(searchString)) {
            return false;
        }
        for (final String tagName : tagsList) {
            if (place.getTags().stream().noneMatch(pt -> pt.getTagName().toLowerCase().equals(tagName))) {
                return false;
            }
        }

        return true;
    }
}
