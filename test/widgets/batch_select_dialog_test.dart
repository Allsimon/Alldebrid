import 'package:alldebrid/widgets/batch_select_dialog.dart';
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

void main() {
  late List<String>? result;

  Future<void> open(WidgetTester tester) async {
    result = null;
    await tester.pumpWidget(
      MaterialApp(
        home: Builder(
          builder: (context) => TextButton(
            onPressed: () async => result = await showBatchSelectDialog(
              context,
              title: 'Show',
              items: const [
                BatchItem(label: 'ep1.mkv', size: 1024, value: 'ep1'),
                BatchItem(label: 'ep2.mkv', size: 2048, value: 'ep2'),
                BatchItem(label: 'sample.mkv', size: 1024, value: 'sample'),
              ],
            ),
            child: const Text('open'),
          ),
        ),
      ),
    );
    await tester.tap(find.text('open'));
    await tester.pumpAndSettle();
  }

  testWidgets('everything starts checked', (tester) async {
    await open(tester);
    expect(find.text('3 of 3 selected'), findsOneWidget);

    await tester.tap(find.text('Download 3 (4 KB)'));
    await tester.pumpAndSettle();
    expect(result, ['ep1', 'ep2', 'sample']);
  });

  testWidgets('returns only the checked items', (tester) async {
    await open(tester);
    await tester.tap(find.text('sample.mkv'));
    await tester.pump();
    expect(find.text('2 of 3 selected'), findsOneWidget);

    await tester.tap(find.text('Download 2 (3 KB)'));
    await tester.pumpAndSettle();
    expect(result, ['ep1', 'ep2']);
  });

  testWidgets('select all toggles everything', (tester) async {
    await open(tester);
    await tester.tap(find.text('3 of 3 selected'));
    await tester.pump();
    expect(find.text('0 of 3 selected'), findsOneWidget);
    expect(
      tester.widget<FilledButton>(find.byType(FilledButton)).onPressed,
      isNull,
    );

    await tester.tap(find.text('0 of 3 selected'));
    await tester.pump();
    expect(find.text('3 of 3 selected'), findsOneWidget);
  });

  testWidgets('cancel returns null', (tester) async {
    await open(tester);
    await tester.tap(find.text('Cancel'));
    await tester.pumpAndSettle();
    expect(result, isNull);
  });
}
