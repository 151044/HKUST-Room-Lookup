package com.s151044.discord.room;

import java.util.Objects;

/**
 * Class representing the minimal amount of information for representing a course section.
 */
public class CourseSection {
    private final String name;
    private final String dept;
    private final String code;
    private final String section;
    private int units = 0;

    /**
     * Constructs a new Course object.
     * @param name The name of the course
     * @param dept The offering department of the course
     * @param code The course code
     * @param section The section of the course
     * @param units The number of credits offered by the course
     */
    public CourseSection(String name, String dept, String code, String section, int units) {
        this.name = name;
        this.dept = dept;
        this.code = code;
        this.section = section;
        this.units = units;
    }

    public String getName() {
        return name;
    }

    public String getDept() {
        return dept;
    }

    public String getCode() {
        return code;
    }

    public String getSection() {
        return section;
    }

    public int getUnits() {
        return units;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CourseSection that = (CourseSection) o;
        return Objects.equals(dept, that.dept) && Objects.equals(code, that.code) && Objects.equals(section, that.section);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dept, code, section);
    }

    @Override
    public String toString() {
        return dept + " " + code + " - " + name + " (" + section + ")";
    }
}
