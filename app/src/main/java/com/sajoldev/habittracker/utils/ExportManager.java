package com.sajoldev.habittracker.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import com.sajoldev.habittracker.data.HabitDao;
import com.sajoldev.habittracker.data.HabitDatabase;
import com.sajoldev.habittracker.data.HabitEntity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class ExportManager {

    private static final int REQUEST_CODE_IMPORT = 1001;
    private static Uri selectedFileUri;

    public static void setSelectedFileUri(Uri uri) {
        selectedFileUri = uri;
    }

    public static void exportToPDF(Context context) {
        new AsyncTask<Void, Void, List<HabitEntity>>() {
            @Override
            protected List<HabitEntity> doInBackground(Void... voids) {
                HabitDao habitDao = HabitDatabase.getInstance(context).habitDao();
                return habitDao.getAllHabitsSync();
            }

            @Override
            protected void onPostExecute(List<HabitEntity> habits) {
                generatePDF(context, habits);
            }
        }.execute();
    }

    private static void generatePDF(Context context, List<HabitEntity> habits) {
        PdfDocument document = new PdfDocument();
        Paint titlePaint = new Paint();
        Paint headerPaint = new Paint();
        Paint textPaint = new Paint();
        Paint linePaint = new Paint();

        titlePaint.setColor(Color.parseColor("#4CAF50"));
        titlePaint.setTextSize(24);
        titlePaint.setFakeBoldText(true);

        headerPaint.setColor(Color.parseColor("#333333"));
        headerPaint.setTextSize(14);
        headerPaint.setFakeBoldText(true);

        textPaint.setColor(Color.parseColor("#666666"));
        textPaint.setTextSize(12);

        linePaint.setColor(Color.parseColor("#E0E0E0"));

        int pageWidth = 595;
        int pageHeight = 842;
        int margin = 40;
        int yPosition = margin;
        int pageNumber = 1;

        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create();
        PdfDocument.Page page = document.startPage(pageInfo);
        Canvas canvas = page.getCanvas();

        canvas.drawText("Habit Tracker Report", margin, yPosition + 24, titlePaint);
        yPosition += 60;

        String dateStr = new SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale.getDefault()).format(new Date());
        canvas.drawText("Generated: " + dateStr, margin, yPosition, textPaint);
        yPosition += 40;

        canvas.drawText("Total Habits: " + habits.size(), margin, yPosition, headerPaint);
        yPosition += 40;

        canvas.drawLine(margin, yPosition, pageWidth - margin, yPosition, linePaint);
        yPosition += 20;

        String[] headers = {"Habit", "Goal", "Category", "Streak", "Completed"};
        int[] colWidths = {150, 120, 100, 60, 70};
        int xPos = margin;

        for (int i = 0; i < headers.length; i++) {
            canvas.drawText(headers[i], xPos, yPosition, headerPaint);
            xPos += colWidths[i];
        }
        yPosition += 25;
        canvas.drawLine(margin, yPosition, pageWidth - margin, yPosition, linePaint);
        yPosition += 20;

        for (HabitEntity habit : habits) {
            if (yPosition > pageHeight - margin - 50) {
                document.finishPage(page);
                pageNumber++;
                pageInfo = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create();
                page = document.startPage(pageInfo);
                canvas = page.getCanvas();
                yPosition = margin;
            }

            xPos = margin;
            String name = habit.getName().length() > 18 ? habit.getName().substring(0, 15) + "..." : habit.getName();
            canvas.drawText(name, xPos, yPosition, textPaint);
            xPos += colWidths[0];

            String goal = habit.getGoal().length() > 15 ? habit.getGoal().substring(0, 12) + "..." : habit.getGoal();
            canvas.drawText(goal, xPos, yPosition, textPaint);
            xPos += colWidths[1];

            canvas.drawText(habit.getCategory() != null ? habit.getCategory() : "-", xPos, yPosition, textPaint);
            xPos += colWidths[2];

            canvas.drawText(String.valueOf(habit.getCurrentStreak()), xPos, yPosition, textPaint);
            xPos += colWidths[3];

            canvas.drawText(String.valueOf(habit.getCompletionDatesList().size()), xPos, yPosition, textPaint);

            yPosition += 30;
        }

        document.finishPage(page);

        try {
            File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            String fileName = "habits_report_" + getTimestamp() + ".pdf";
            File file = new File(downloadsDir, fileName);

            FileOutputStream outputStream = new FileOutputStream(file);
            document.writeTo(outputStream);
            document.close();
            outputStream.close();

            Toast.makeText(context, "Saved to Downloads: " + fileName, Toast.LENGTH_LONG).show();

            Uri uri = FileProvider.getUriForFile(context, context.getPackageName() + ".provider", file);
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("application/pdf");
            shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            context.startActivity(Intent.createChooser(shareIntent, "Share PDF"));

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(context, "Failed to export PDF", Toast.LENGTH_SHORT).show();
        }
    }

    public static void exportToJSON(Context context) {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... voids) {
                HabitDao habitDao = HabitDatabase.getInstance(context).habitDao();
                List<HabitEntity> habits = habitDao.getAllHabitsSync();

                try {
                    JSONObject root = new JSONObject();
                    root.put("exportDate", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()));
                    root.put("totalHabits", habits.size());

                    JSONArray habitsArray = new JSONArray();
                    for (HabitEntity habit : habits) {
                        JSONObject habitObj = new JSONObject();
                        habitObj.put("id", habit.getId());
                        habitObj.put("name", habit.getName());
                        habitObj.put("goal", habit.getGoal());
                        habitObj.put("iconResourceId", habit.getIconResourceId());
                        habitObj.put("color", habit.getColor());
                        habitObj.put("category", habit.getCategory());
                        habitObj.put("frequency", habit.getFrequency());
                        habitObj.put("selectedDays", habit.getSelectedDays());
                        habitObj.put("currentStreak", habit.getCurrentStreak());
                        habitObj.put("longestStreak", habit.getLongestStreak());
                        habitObj.put("createdDate", DateUtils.dateToString(habit.getCreatedDate()));

                        JSONArray completedDates = new JSONArray();
                        for (String date : habit.getCompletionDatesList()) {
                            completedDates.put(date);
                        }
                        habitObj.put("completedDates", completedDates);

                        habitsArray.put(habitObj);
                    }

                    root.put("habits", habitsArray);
                    return root.toString(2);

                } catch (JSONException e) {
                    e.printStackTrace();
                    return null;
                }
            }

            @Override
            protected void onPostExecute(String jsonContent) {
                if (jsonContent != null) {
                    saveJSONFile(context, jsonContent);
                } else {
                    Toast.makeText(context, "Export failed", Toast.LENGTH_SHORT).show();
                }
            }
        }.execute();
    }

    public static void importFromJSON(Context context, Uri fileUri) {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... voids) {
                try {
                    InputStream inputStream = context.getContentResolver().openInputStream(fileUri);
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                    StringBuilder stringBuilder = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        stringBuilder.append(line);
                    }
                    reader.close();
                    inputStream.close();
                    return stringBuilder.toString();
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
            }

            @Override
            protected void onPostExecute(String jsonContent) {
                if (jsonContent == null) {
                    Toast.makeText(context, "Failed to read file", Toast.LENGTH_SHORT).show();
                    return;
                }
                processImport(context, jsonContent);
            }
        }.execute();
    }

    private static void processImport(Context context, String jsonContent) {
        try {
            JSONObject root = new JSONObject(jsonContent);
            JSONArray habitsArray = root.getJSONArray("habits");

            new AsyncTask<Void, Void, List<HabitEntity>>() {
                @Override
                protected List<HabitEntity> doInBackground(Void... voids) {
                    HabitDao habitDao = HabitDatabase.getInstance(context).habitDao();
                    return habitDao.getAllHabitsSync();
                }

                @Override
                protected void onPostExecute(List<HabitEntity> existingHabits) {
                    if (existingHabits.size() > 0) {
                        showImportWarningDialog(context, habitsArray, existingHabits.size(), true);
                    } else {
                        performImport(context, habitsArray, true);
                    }
                }
            }.execute();

        } catch (JSONException e) {
            Toast.makeText(context, "Invalid JSON file", Toast.LENGTH_SHORT).show();
        }
    }

    private static void showImportWarningDialog(Context context, JSONArray habitsArray, int existingCount, boolean replace) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Existing Data Found");
        builder.setMessage("You already have " + existingCount + " habits in your app. What would you like to do?\n\n• Replace: Delete existing data and import from backup\n• Merge: Keep existing data and add imported data");
        builder.setPositiveButton("Replace", (dialog, which) -> {
            performImport(context, habitsArray, true);
        });
        builder.setNeutralButton("Merge", (dialog, which) -> {
            performImport(context, habitsArray, false);
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> {
            Toast.makeText(context, "Import cancelled", Toast.LENGTH_SHORT).show();
        });
        builder.show();
    }

    private static void performImport(Context context, JSONArray habitsArray, boolean replace) {
        new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... voids) {
                try {
                    HabitDao habitDao = HabitDatabase.getInstance(context).habitDao();

                    if (replace) {
                        habitDao.deleteAllHabits();
                    }

                    for (int i = 0; i < habitsArray.length(); i++) {
                        JSONObject habitObj = habitsArray.getJSONObject(i);

                        int iconResId = habitObj.optInt("iconResourceId", android.R.drawable.ic_menu_edit);
                        if (iconResId == 0) {
                            iconResId = android.R.drawable.ic_menu_edit;
                        }

                        int habitColor = habitObj.optInt("color", 0xFF4CAF50);
                        if (habitColor == 0) {
                            habitColor = 0xFF4CAF50;
                        }

                        HabitEntity habit = new HabitEntity(
                                habitObj.optString("name", "Imported Habit"),
                                habitObj.optString("goal", ""),
                                habitColor,
                                iconResId
                        );

                        habit.setCategory(habitObj.optString("category", "Other"));
                        habit.setFrequency(habitObj.optString("frequency", "Daily"));
                        habit.setSelectedDays(habitObj.optString("selectedDays", ""));
                        habit.setCurrentStreak(habitObj.optInt("currentStreak", 0));
                        habit.setLongestStreak(habitObj.optInt("longestStreak", 0));

                        String createdDateStr = habitObj.optString("createdDate", "");
                        if (!createdDateStr.isEmpty()) {
                            habit.setCreatedDate(DateUtils.stringToDate(createdDateStr));
                        }

                        JSONArray completedDates = habitObj.optJSONArray("completedDates");
                        if (completedDates != null) {
                            Set<String> datesSet = new HashSet<>();
                            for (int j = 0; j < completedDates.length(); j++) {
                                datesSet.add(completedDates.getString(j));
                            }
                            habit.setCompletedDates(datesSet);
                        }

                        habitDao.insertHabit(habit);
                    }

                    return true;
                } catch (JSONException e) {
                    e.printStackTrace();
                    return false;
                }
            }

            @Override
            protected void onPostExecute(Boolean success) {
                if (success) {
                    Toast.makeText(context, "Import successful!", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(context, "Import failed", Toast.LENGTH_SHORT).show();
                }
            }
        }.execute();
    }

    private static void saveJSONFile(Context context, String content) {
        try {
            File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File file = new File(downloadsDir, "habits_backup_" + getTimestamp() + ".json");

            FileWriter writer = new FileWriter(file);
            writer.write(content);
            writer.close();

            Toast.makeText(context, "Saved to Downloads: " + file.getName(), Toast.LENGTH_LONG).show();

            Uri uri = FileProvider.getUriForFile(context, context.getPackageName() + ".provider", file);
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("application/json");
            shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            context.startActivity(Intent.createChooser(shareIntent, "Share JSON"));

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(context, "Failed to save file", Toast.LENGTH_SHORT).show();
        }
    }

    private static String getTimestamp() {
        return new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
    }
}
