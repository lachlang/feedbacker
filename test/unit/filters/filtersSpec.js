'use strict';

describe('review cycle filter [reviewCycle]', function() {

  var $filter;

  beforeEach(module('feedbacker'));

  beforeEach(inject(function(_$filter_){
    $filter = _$filter_;
  }));

  describe('review cycle filter', function() {

    it('should return an empty list when not provided an array as an input', function() {
      var result = $filter('reviewCycle')({"some":"thing"})
      expect(result).toEqual([]);
    });

    it('should return the input when given an undefined cycle id', function() {
      var result, input = [{"some": "thing"}, {"some": "other thing"}];
      result = $filter('reviewCycle')(input);
      expect(result).toBe(input);
      result = $filter('reviewCycle')(input, {"some": "thing"});
      expect(result).toBe(input);
    });

    it('should return the input not given an object cycle id', function() {
      var result, input = [{"some": "thing"}, {"some": "other thing"}];
      result = $filter('reviewCycle')(input);
      expect(result).toBe(input);
    });

    it('should return the input when not given a floating point cycle id', function() {
      var result, input = [{"some": "thing"}, 12.3];
      result = $filter('reviewCycle')(input);
      expect(result).toBe(input);
    });

    it('should filter all input values when they do not contain a cycle id', function() {
      var result, input = [{"some": "thing"}, {"some": "other thing"}];
      result = $filter('reviewCycle')(input, 12);
      expect(result).toEqual([]);
    });

    it('should filter all input values', function() {
      var result, input = [{"some": "thing", 'cycleId':11},{"some": "other thing",'cycleId':13}];
      result = $filter('reviewCycle')(input, 12);
      expect(result).toEqual([]);
    });

    it('should match all input values', function() {
      var result, input = [{"some": "thing", "cycleId": 12}, {"some": "other thing", "cycleId":12}];
      result = $filter('reviewCycle')(input, 12);
      expect(result).toEqual(input);
    });

    it('should match one input value', function() {
      var result, input = [{"some": "thing", "cycleId": 12}, {"some": "other thing", "cycleId":13}];

      result = $filter('reviewCycle')(input, 12);

      expect(result).toEqual([{"some": "thing", "cycleId": 12}]);
    });
  });

	describe('report display filter', function() {

        it('should return an empty list when not provided an array as an input', function() {
            var result = $filter('reportDisplay')({"some":"thing"})
            expect(result).toEqual([]);
        });

        it('should return the input when not given an object filter switch', function() {
            var result, input = [{"some": "thing"}, {"some": "other thing"}];
            result = $filter('reportDisplay')(input, {"some": "thing"});
            expect(result).toBe(input);
        });

        it('should return the input when not given an undefined filter switch', function() {
            var result, input = [{"some": "thing"}, {"some": "other thing"}];
            result = $filter('reportDisplay')(input);
            expect(result).toBe(input);
        });

        it('should return the input when  given a floading point filter swtich', function() {
            var result, input = [{"some": "thing"}, 12.3];
            result = $filter('reportDisplay')(input);
            expect(result).toBe(input);
        });

        it('should return the input when  given an integer filter switch', function() {
            var result, input = [{"some": "thing"}, 12];
            result = $filter('reportDisplay')(input);
            expect(result).toBe(input);
        });

        it('should return all input values when the filter switch is a random string', function() {
            var result, input = [{"cycle":{"some": "thing", "active": true}}, {"cycle":{"some": "other thing", "active":false}}];
            result = $filter('reportDisplay')(input, '');
            expect(result).toEqual(input);
            result = $filter('reportDisplay')(input, 'all');
            expect(result).toEqual(input);
            result = $filter('reportDisplay')(input, 'cats');
            expect(result).toEqual(input);
        });

        it('should filter all input values', function() {
            var result, input = [{"cycle":{"some": "thing", "active": false}}, {"cycle":{"some": "other thing", "active":false}}];
            result = $filter('reportDisplay')(input, 'current');
            expect(result).toEqual([]);
            result = $filter('reportDisplay')(input, 'Current');
            expect(result).toEqual([]);
            result = $filter('reportDisplay')(input, 'CURRENT');
            expect(result).toEqual([]);
        });

        it('should match one input value', function() {
            var result, input = [{"cycle":{"id":1, "some": "thing", "active": true}}, {"cycle":{"id":2, "some": "other thing", "active":false}}];
            result = $filter('reportDisplay')(input, 'CURRENT');
            expect(result).toEqual([{"cycle":{"id":1, "some": "thing", "active": true}}]);
            result = $filter('reportDisplay')(input, 'cURRent');
            expect(result).toEqual([{"cycle":{"id":1, "some": "thing", "active": true}}]);
        });

		it('should match all input values', function() {
            var result, input = [{"cycle":{"some": "thing", "active": true}}, {"cycle":{"some": "other thing", "active":true}}];
			result = $filter('reportDisplay')(input, 'current');
			expect(result).toEqual(input);
		});
    });
});