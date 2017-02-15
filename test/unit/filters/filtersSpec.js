'use strict';

describe('review cycle filter [reviewCycle]', function() {

	var $filter;

	beforeEach(module('feedbacker.filters'));

	beforeEach(inject(function(_$filter_){
		$filter = _$filter_;
	}));

	describe('review cycle filter', function() {

        it('should return an empty list when not provided an array as an input', function() {
            var result = $filter('reviewCycle')({"some":"thing"})
            expect(result).toEqual([]);
        });

        it('should return the input when not given an undefined cycle id', function() {
            var result, input = [{"some": "thing"}, {"some": "other thing"}];
            result = $filter('reviewCycle')(input);
            expect(result).toBe(input);
            result = $filter('reviewCycle')(input, {"some": "thing"});
            expect(result).toBe(input);
        });

        it('should return the input when not given an object valid cycle id', function() {
            var result, input = [{"some": "thing"}, {"some": "other thing"}];
            result = $filter('reviewCycle')(input);
            expect(result).toBe(input);
        });

        it('should return the input when not given a floading point cycle id', function() {
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
});