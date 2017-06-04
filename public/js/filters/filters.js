fbFilters.filter('reviewCycle', ['Util', function(Util) {

  return function(input, reviewCycleId) {
    if (!Array.isArray(input)) {
      return [];
    } else if (!reviewCycleId || !Util.isInteger(reviewCycleId)) {
      return input;
    } else {
      return input.filter(function (item) {
        return item.cycleId == reviewCycleId;
      });
    }
  };
}]);

fbFilters.filter('reportDisplay', function() {
  return function(input, filterToggle) {
    if (!Array.isArray(input)) {
      return [];
    } else if (filterToggle &&
              (typeof filterToggle === 'string' || filterToggle instanceof String) &&
              (filterToggle.toLowerCase() === 'current' || filterToggle.toLowerCase() === 'active')){ // return current reports
      return input.filter(function(item) {
        return item.cycle && item.cycle.active;
      });
    } else if (filterToggle &&
              (typeof filterToggle === 'string' || filterToggle instanceof String) &&
              (filterToggle.toLowerCase() === 'inactive')) {
      return input.filter(function(item) {
        return item.cycle && !item.cycle.active;
      });
    } else {
      return input; // return 'all'
    }
  };
});
