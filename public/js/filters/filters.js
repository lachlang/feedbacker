fbFilters.filter('reviewCycle', function() {
    return function(input, reviewCycleId) {
        if (!Array.isArray(input)) {
            return [];
        } else if (!reviewCycleId || !Number.isInteger(reviewCycleId)) {
            return input;
        } else {
            return input.filter(function (item) {
                return item.cycleId == reviewCycleId;
            });
        }
    };
});

fbFilters.filter('reportDisplay', function() {
    return function(input, filterToggle) {
        if (!Array.isArray(input)) {
            return [];
        } else if (filterToggle &&
                    (typeof filterToggle === 'string' || filterToggle instanceof String) &&
                    filterToggle.toLowerCase() == 'current'){ // return current reports
            return input.filter(function(item) {
                return item.cycle && item.cycle.active;
            });
        } else {
            return input; // return 'all'
        }
    };
});
