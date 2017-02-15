fbFilters.filter('reviewCycle', function() {
    return function(input, reviewCycleId) {
        if (!Array.isArray(input)) {
            return [];
        } else if (!reviewCycleId || !Number.isInteger(reviewCycleId)) {
            return input;
        }
        return input.filter(function (item) {
            return item.cycleId == reviewCycleId;
        });
    };
});
