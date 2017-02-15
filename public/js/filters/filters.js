fbFilters.filter('reviewCycle', function() {
    return function(input, reviewCycleId) {
        if (!Array.isArray(input)) return [];
        return input.filter(function (item) {
            return item.cycleId == reviewCycleId;
        });
    };
});